package com.pryme.Backend.eligibility.audit;

import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import java.math.BigDecimal;

public record AuditComparisonRequest(
    EligibilityRequest profile,
    ExpectedResult expected
) {
    public record ExpectedResult(
        String expectedProgram,        // e.g. "NIP", "LOW_LTV", "GST", etc.
        BigDecimal expectedAmount,
        BigDecimal expectedFoir,
        BigDecimal expectedLtv,
        BigDecimal expectedRoi,
        BigDecimal expectedIncome,
        Boolean expectedEligible
    ) {}
}
