package com.pryme.Backend.eligibility.dto;
import java.util.List;

public record PreflightResult(boolean passed, List<String> violations) {
    public List<String> getViolations() { return violations; }
}
