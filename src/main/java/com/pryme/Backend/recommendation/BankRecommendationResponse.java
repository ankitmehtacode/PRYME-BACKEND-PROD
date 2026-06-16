package com.pryme.Backend.recommendation;

import java.math.BigDecimal;

public record BankRecommendationResponse(
        Long loanProductId,
        Long lenderId,
        String lenderName,
        BigDecimal roi,
        BigDecimal processingFee,
        BigDecimal loginFee,
        String loanType,
        BigDecimal fitScore,
        String adminFee,
        String insuranceCharges,
        String legalTechnicalCharges,
        String otherExpense,
        String stampDuty,
        String prepaymentCharges,
        String foreclosureCharges
) {
}
