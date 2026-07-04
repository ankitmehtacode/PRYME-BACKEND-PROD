package com.pryme.Backend.eligibility.audit;

import java.math.BigDecimal;
import java.util.Map;

public record FormulaTrace(
    String formulaName,
    String expression,
    Map<String, Object> inputs,
    BigDecimal output
) {}
