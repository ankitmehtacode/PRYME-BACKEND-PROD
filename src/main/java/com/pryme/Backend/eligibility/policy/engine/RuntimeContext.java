package com.pryme.Backend.eligibility.policy.engine;

import java.time.Instant;

public record RuntimeContext(
    String traceId,
    String bundleId,
    String policyVersion,
    String engineVersion,
    String certificationId,
    String tenantId,
    Instant requestTime
) {}
