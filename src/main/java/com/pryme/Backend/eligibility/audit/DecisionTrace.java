package com.pryme.Backend.eligibility.audit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DecisionTrace(
    UUID traceId,
    Instant executedAt,
    String engineVersion,
    String masterDataVersion,
    long totalExecutionMillis,
    EligibilityRequestSnapshot requestSnapshot,
    List<DecisionStep> steps,
    DecisionSummary summary
) {}
