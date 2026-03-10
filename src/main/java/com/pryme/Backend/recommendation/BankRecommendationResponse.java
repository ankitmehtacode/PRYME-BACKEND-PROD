package com.pryme.Backend.recommendation;

import java.math.BigDecimal;
import java.util.UUID;

public record BankRecommendationResponse(
        UUID loanProductId,
        UUID bankId,
        String bankName,
        String logoUrl,
        BigDecimal interestRate,
        BigDecimal processingFee,
        String loanType,
        BigDecimal fitScore
) {
}
