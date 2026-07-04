package com.pryme.Backend.eligibility.audit.certification;

import java.math.BigDecimal;

public class WorkbookModels {

    public record EligibilityRow(
        String productName,
        String loanType,
        String lenderName,
        String interestType,
        String propertyType,
        String negativeProperty,
        String employmentType,
        String selfEmployedProfessional,
        String surrogate,
        String marginByOccupation,
        String ltv,
        String formulae,
        String conditions,
        Integer minCibil,
        Integer minTenure,
        Integer maxTenure,
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        String negativeEmployerType,
        BigDecimal minIncome,
        Integer minAge,
        Integer maxAge,
        String negativeProfile,
        String vintage,
        String ITRRequired,
        String providentFundMandatory,
        String negativeModeSalary,
        String bankStatement,
        String salarySlipMonths,
        String gstRequiredMonths,
        String emiNotObligated,
        String adminFee,
        String insuranceCharges,
        String legalTechnicalCharges,
        String otherExpense,
        String stampDuty,
        String prepaymentCharges,
        String foreclosureCharges,
        String notes
    ) {}

    public record FoirRow(
        String productName,
        String loanType,
        String lenderName,
        String surrogate,
        String employmentType,
        BigDecimal lowerSalary,
        BigDecimal upperSalary,
        BigDecimal foir,
        String deviation
    ) {}

    public record PfRow(
        String productName,
        String loanType,
        String lenderName,
        String employmentType,
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        BigDecimal pf,
        BigDecimal tax,
        String notes
    ) {}

    public record LoginFeeRow(
        String productName,
        String loanType,
        String lenderName,
        String employmentType,
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        BigDecimal loginFees
    ) {}

    public record HlLtvRow(
        String propertyType, // "Ready Built Property" or "Plot"
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        BigDecimal ltv
    ) {}

    public record LapLtvRow(
        String lenderName,
        String propertyCategory, // "Residential" or "Commercial"
        String propertySubtype,  // "Plot", "Flat/Apartment/House", etc.
        String ltvValue         // "Negative" or a number like "0.75"
    ) {}
}
