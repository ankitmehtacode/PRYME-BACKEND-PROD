package com.pryme.Backend.eligibility.policy.model;

import java.time.Instant;

public record BundleMetadata(
    String uploadedBy,
    String approvedBy,
    Instant effectiveDate,
    Instant expiryDate,
    String description,
    String remarks
) {}
