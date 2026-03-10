package com.pryme.Backend.recommendation;

import java.math.BigDecimal;

public record ProductCardResponse(
        String id,
        String label,
        String tag,
        String href,
        String accent,
        BigDecimal bestInterestRate,
        BigDecimal bestProcessingFee
) {
}
