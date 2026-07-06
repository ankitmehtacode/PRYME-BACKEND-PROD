package com.pryme.Backend.eligibility.policy.model;

import java.math.BigDecimal;

public record FoirPolicyRule(
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
