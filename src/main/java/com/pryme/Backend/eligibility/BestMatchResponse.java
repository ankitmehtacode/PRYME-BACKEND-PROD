package com.pryme.Backend.eligibility;

import java.util.List;
import com.pryme.Backend.eligibility.dto.EligibilityResult;

public record BestMatchResponse(
        EligibilityResult bestMatch,
        List<EligibilityResult> evaluated
) {
}
