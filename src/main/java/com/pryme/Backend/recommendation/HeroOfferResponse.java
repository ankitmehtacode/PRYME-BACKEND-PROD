package com.pryme.Backend.recommendation;

import java.math.BigDecimal;
import java.util.UUID;

public record HeroOfferResponse(
        String id,
        UUID loanProductId,
        UUID bankId,
        String bank,
        String logo,
        String title,
        String desc,
        String tag,
        BigDecimal interestRate,
        BigDecimal processingFee,
        String loanType
) {
}
