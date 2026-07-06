package com.pryme.Backend.eligibility.policy.model;

import java.time.Instant;

public record BundleSignature(
    String bundleHash,
    String manifestHash,
    String gitCommit,
    String engineVersion,
    String policyVersion,
    String signature,
    Instant createdAt
) {}
