package com.pryme.Backend.recommendation;

import java.math.BigDecimal;

public record BankRecommendationResponse(
        Long loanProductId,
        Long lenderId,
        String lenderName,
        BigDecimal roi,
        BigDecimal processingFee,
        String loanType,
        BigDecimal fitScore
) {
}
