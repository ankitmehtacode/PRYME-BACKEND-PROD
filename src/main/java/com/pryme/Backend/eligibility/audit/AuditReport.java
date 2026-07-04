package com.pryme.Backend.eligibility.audit;

import java.time.Instant;
import java.util.List;

public record AuditReport(
    List<DecisionTrace> traces,
    String masterDataVersion,
    String engineVersion,
    long totalExecutionMillis,
    Instant generatedAt,
    String requestHash
) {}
