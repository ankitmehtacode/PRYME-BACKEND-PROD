package com.pryme.Backend.eligibility.policy.model;

import java.math.BigDecimal;

public record ProductRoiMatrixRule(
    Long productId,
    String employmentType,
    BigDecimal minLoanAmount,
    BigDecimal maxLoanAmount,
    Integer minCibil,
    Integer maxCibil,
    boolean ntc,
    BigDecimal roi
) {}
