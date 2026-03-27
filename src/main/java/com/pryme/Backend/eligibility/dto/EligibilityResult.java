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
}
