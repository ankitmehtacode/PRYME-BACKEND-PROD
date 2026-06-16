package com.pryme.Backend.eligibility.dto;

import java.math.BigDecimal;
import java.util.List;

public record EligibilityResult(
        boolean eligible,
        String productCode,
        String productName,
        String programName,
        BigDecimal computedMonthlyIncome,
        BigDecimal effectiveFoir,
        BigDecimal proposedEmi,
        BigDecimal maxEligibleAmount,
        BigDecimal roi,
        int tenureMonths,
        BigDecimal ltv,
        boolean ltvDeviated,
        List<String> rejectionReasons,
        String notes,
        BigDecimal processingFee,
        BigDecimal loginFee,

        // Fee Structure
        String adminFee,
        String insuranceCharges,
        String legalTechnicalCharges,
        String otherExpense,
        String stampDuty,
        String prepaymentCharges,
        String foreclosureCharges,

        // Eligibility Details
        String vintage,
        String negativeProperty,
        String negativeEmployerType,
        String negativeSalaryMode,
        String marginByOccupation,
        String deviationFormulae,
        String conditions,
        String emiNotObligated,
        String bankStatementRequirement,
        String salarySlipRequirement,
        String gstReturnRequirement,
        Boolean providentFundMandatory,
        Integer itrRequiredYears,
        String profileRestrictions
) {
    // Backwards-compatibility bridge for existing controllers/services
    public boolean isEligible() {
        return this.eligible;
    }

    // Restored Static Factories
    public static EligibilityResult rejected(List<String> reasons, String notes) {
        return new EligibilityResult(
                false, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                false, reasons, notes, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
    }

    public static EligibilityResult ineligible(String productCode, String productName, List<String> reasons, String notes) {
        return new EligibilityResult(
                false, productCode, productName, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                false, reasons, notes, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
    }

    public EligibilityResult toPublicResult() {
        // Obfuscate / simplify productCode (e.g., ABFL-HL-0001 -> ABFL-HL)
        String publicProductCode = null;
        if (this.productCode != null) {
            String[] parts = this.productCode.split("-");
            if (parts.length >= 2) {
                publicProductCode = parts[0] + "-" + parts[1];
            } else {
                publicProductCode = this.productCode;
            }
        }

        // Map and clean rejection reasons to professional, non-backtrackable guidelines
        List<String> publicRejectionReasons = null;
        if (this.rejectionReasons != null) {
            publicRejectionReasons = this.rejectionReasons.stream()
                    .map(EligibilityResult::sanitizeReason)
                    .distinct()
                    .toList();
        }

        // Clean notes (remove internal Low LTV terminology)
        String publicNotes = this.notes;
        if (publicNotes != null) {
            if (publicNotes.contains("Low LTV")) {
                publicNotes = "Eligible";
            }
        }

        return new EligibilityResult(
                this.eligible,
                publicProductCode,
                this.productName, // Lender name (clean display name, e.g. "Aditya Birla Finance Limited")
                null, // programName nullified to prevent backtrack
                this.computedMonthlyIncome,
                this.effectiveFoir,
                this.proposedEmi,
                this.maxEligibleAmount,
                this.roi,
                this.tenureMonths,
                this.ltv,
                this.ltvDeviated,
                publicRejectionReasons,
                publicNotes,
                this.processingFee,
                this.loginFee,

                // Fee Structure nullified to prevent backtrack
                null, null, null, null, null, null, null,

                // Eligibility Details nullified to prevent backtrack
                null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
    }

    public static String sanitizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Applicant profile does not meet standard eligibility criteria.";
        }
        String upper = reason.toUpperCase();
        if (upper.startsWith("AGE_TOO_LOW")) {
            return "Applicant age is below the minimum required age.";
        }
        if (upper.startsWith("AGE_TOO_HIGH")) {
            return "Applicant age is above the maximum allowed age.";
        }
        if (upper.startsWith("BIZ_VINTAGE_LOW")) {
            return "Business vintage (years in business) is below the minimum requirement.";
        }
        if (upper.startsWith("WORK_EXP_LOW")) {
            return "Work experience is below the minimum required duration.";
        }
        if (upper.startsWith("PROPERTY_NOT_ALLOWED")) {
            return "Property type is not eligible for this product.";
        }
        if (upper.startsWith("CITY_TIER_MISMATCH")) {
            return "Product is not available in the specified city tier.";
        }
        if (upper.startsWith("CIBIL_TOO_LOW")) {
            return "CIBIL score is below the minimum required threshold.";
        }
        if (upper.startsWith("ITR_YEARS_LOW")) {
            return "Available ITR history is below the minimum required years.";
        }
        if (upper.startsWith("PROFILE_RESTRICTED")) {
            return "Employment profile does not meet the lender's guidelines.";
        }
        if (upper.startsWith("NEGATIVE_PROPERTY")) {
            return "Property is in a restricted category or location.";
        }
        if (upper.startsWith("CONDITION_RULE_FAILED")) {
            return "Applicant profile does not satisfy lender internal policy conditions.";
        }
        if (upper.startsWith("DEVIATION_RULE_FAILED")) {
            return "Applicant profile deviates from standard eligibility parameters.";
        }
        if (upper.startsWith("NO ELIGIBILITY LANE") || upper.contains("NO ELIGIBILITY LANE")) {
            return "Applicant profile is not eligible for this product under current parameters.";
        }
        if (upper.startsWith("COMPUTED INCOME") || upper.contains("BELOW MINIMUM")) {
            return "Calculated income does not meet the minimum required floor.";
        }
        if (upper.startsWith("FOIR EXCEEDED") || upper.contains("FOIR LIMIT")) {
            return "EMI obligations exceed the maximum allowable debt-to-income ratio (FOIR).";
        }
        if (upper.contains("SERVICE AREA RESTRICTED") || upper.contains("OUTSIDE INDORE")) {
            return "PRYME currently operates only in Indore (PIN 452xxx/453xxx).";
        }
        if (upper.contains("NO MATCHING PRODUCTS")) {
            return "No matching products found for the specified CIBIL score.";
        }
        
        // Fallback for anything else
        return "Applicant profile does not meet the lender's eligibility criteria.";
    }
}