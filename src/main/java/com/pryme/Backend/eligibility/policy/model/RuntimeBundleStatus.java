package com.pryme.Backend.eligibility.policy.model;

import java.time.Instant;
import java.time.Duration;

public record RuntimeBundleStatus(
    String bundleId,
    boolean cacheWarm,
    Instant activatedAt,
    Instant warmedAt,
    HealthStatus status,
    long policyCount,
    Duration activationAge
) {}
