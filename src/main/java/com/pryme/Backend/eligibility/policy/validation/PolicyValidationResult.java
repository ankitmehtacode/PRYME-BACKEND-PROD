package com.pryme.Backend.eligibility.policy.validation;

import java.util.List;
import java.util.Map;

public record PolicyValidationResult(
    boolean pass,
    String severity, // "ERROR", "WARNING", "INFO"
    List<String> errors,
    List<String> warnings,
    List<String> duplicates,
    List<String> overlaps,
    List<String> conflicts,
    List<String> orphans,
    Map<String, Object> statistics,
    Map<String, Double> coverage
) {}
