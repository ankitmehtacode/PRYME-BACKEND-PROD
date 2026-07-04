package com.pryme.Backend.eligibility.audit;

import java.math.BigDecimal;
import java.util.List;

public record DecisionStep(
    ProgramType program,
    DecisionStatus status,
    long executionMillis,
    
    // Condition lane that matched (null if none matched)
    Long matchedConditionId,
    String matchedEmploymentType,
    String matchedSurrogate,
    
    // Computed values (null if stage didn't reach computation)
    BigDecimal computedIncome,
    BigDecimal effectiveFoir,
    BigDecimal effectiveRoi,
    BigDecimal proposedEmi,
    BigDecimal maxEligibleAmount,
    
    // LTV detail
    LtvDetail ltvDetail,
    
    // Fee resolution
    BigDecimal processingFee,
    BigDecimal loginFee,
    
    // Rule-level evaluations
    List<RuleEvaluation> rules,
    
    // Formula traces
    List<FormulaTrace> formulas
) {}
