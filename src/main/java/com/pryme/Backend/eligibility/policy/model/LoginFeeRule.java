package com.pryme.Backend.eligibility.policy.model;

import java.math.BigDecimal;

public record LoginFeeRule(
    String productName,
    String loanType,
    String lenderName,
    String employmentType,
    BigDecimal minLoanAmount,
    BigDecimal maxLoanAmount,
    BigDecimal loginFees
) {}
