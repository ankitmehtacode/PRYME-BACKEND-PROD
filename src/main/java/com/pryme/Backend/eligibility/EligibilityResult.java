package com.pryme.Backend.eligibility;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EligibilityResult(
        LapProgram program,
        BigDecimal eligibleIncome,
        BigDecimal maxEmiAllowed,
        BigDecimal netEligibleEmi,
        BigDecimal foirPercent,
        BigDecimal deviationPercent,
        String formula,
        boolean eligible,
        List<String> reasons,
        BigDecimal minimumLoanAmount,
        BigDecimal maximumLoanAmount,
        BigDecimal estimatedEligibleLoanAmount,
        Integer emiNotObligatedMonths,
        String conditions,
        String specialNotes,
        LocalDate lastUpdatedOn
) {
}
