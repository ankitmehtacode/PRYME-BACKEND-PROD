package com.pryme.Backend.eligibility.audit;

import java.math.BigDecimal;
import java.util.Map;

public record FormulaTrace(
    String formulaName,
    String formulaVersion,
    String expression,
    Map<String, Object> inputs,
    Map<String, Object> intermediateVariables,
    String rounding,
    int scale,
    BigDecimal output
) {}

