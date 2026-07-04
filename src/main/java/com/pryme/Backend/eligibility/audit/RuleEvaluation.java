package com.pryme.Backend.eligibility.audit;

public record RuleEvaluation(
    String ruleName,
    DecisionStatus status,
    Object expected,
    Object actual,
    String message
) {}
