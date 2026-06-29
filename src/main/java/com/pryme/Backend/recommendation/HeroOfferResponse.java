package com.pryme.Backend.recommendation;

import java.math.BigDecimal;

public record HeroOfferResponse(
        String id,
        Long loanProductId,
        Long lenderId,
        String lenderName,
        String title,
        String desc,
        String tag,
        BigDecimal roi,
        BigDecimal processingFee,
        BigDecimal loginFee,
        String loanType,
        String logoType,
        String bannerImageUrl,
        String heroImageUrl,
        String targetUrl
) {
}
