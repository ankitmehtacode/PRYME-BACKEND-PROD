package com.pryme.Backend.eligibility.audit;

import java.math.BigDecimal;

public record EligibilityRequestSnapshot(
    Integer cibilScore,
    int applicantAge,
    String employmentType,
    String propertyType,
    BigDecimal loanAmount,
    BigDecimal propertyValue,
    int requestedTenureMonths,
    BigDecimal monthlyIncome,
    BigDecimal existingEmiTotal,
    String loanType,
    String programName
) {}
