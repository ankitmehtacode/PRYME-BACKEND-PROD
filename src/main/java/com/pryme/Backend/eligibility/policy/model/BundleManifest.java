package com.pryme.Backend.eligibility.policy.model;

import java.time.Instant;
import java.util.Map;

public record BundleManifest(
    String bundleId,
    String version,
    String policyBundleHash,
    Map<String, String> individualHashes,
    String gitCommit,
    String certificationId,
    PolicyState state,
    boolean active,
    Instant createdTime
) {}
