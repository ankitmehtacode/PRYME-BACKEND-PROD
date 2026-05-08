package com.pryme.Backend.eligibility.dto;

import java.math.BigDecimal;

/**
 * Lightweight value object carrying the minimal applicant dimensions
 * required for product matching and dynamic pricing.
 *
 * Fields intentionally mirror the parameters of
 * {@link com.pryme.Backend.eligibility.service.FinancialComputationEngine#resolveInterestRate}
 * so the recommendation pipeline can hydrate pricing without extra mapping.
 *
 * @param cibil     CIBIL TransUnion score (300–900)
 * @param empType   employment classification ('SALARIED', 'SEP', 'FREELANCE', etc.)
 * @param monthlyIncome gross monthly income in ₹ — used for future FOIR gating
 */
public record ApplicantProfile(
        int cibil,
        String empType,
        BigDecimal monthlyIncome
) {
    /**
     * Compact constructor enforcing invariants.
     */
    public ApplicantProfile {
        if (cibil < 300 || cibil > 900) {
            throw new IllegalArgumentException(
                    "CIBIL score must be in [300, 900]; received: " + cibil);
        }
    }
}
