package com.pryme.Backend.eligibility;

import java.util.List;

public record BestMatchResponse(
        EligibilityResult bestMatch,
        List<EligibilityResult> evaluated
) {
}
