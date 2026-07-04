package com.pryme.Backend.eligibility.audit;

import java.math.BigDecimal;

public record DecisionSummary(
    DecisionStatus finalStatus,
    ProgramType selectedProgram,
    BigDecimal finalEligibleAmount,
    BigDecimal finalRoi,
    BigDecimal finalLtv,
    int stagesTried,
    int stagesPassed,
    int stagesFailed
) {}
