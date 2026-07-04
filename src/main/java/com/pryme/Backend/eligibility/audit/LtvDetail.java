package com.pryme.Backend.eligibility.audit;

import java.math.BigDecimal;

public record LtvDetail(
    BigDecimal propertyValue,
    BigDecimal ltvPercentage,
    String formula,
    BigDecimal eligibleLoanAmount,
    String source
) {}
