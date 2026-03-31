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
        String notes
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
                false, reasons, notes
        );
    }

    public static EligibilityResult ineligible(String productCode, String productName, List<String> reasons, String notes) {
        return new EligibilityResult(
                false, productCode, productName, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                false, reasons, notes
        );
    }
}