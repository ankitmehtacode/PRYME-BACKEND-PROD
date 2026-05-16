package com.pryme.Backend.eligibility.dto;

import java.math.BigDecimal;

/**
 * Lightweight value object carrying the minimal applicant dimensions
 * required for product matching and dynamic pricing.
 *
 * Fields carry the minimal applicant dimensions required for product
 * matching and pricing hydration in the recommendation pipeline.
 *
 * @param cibil     CIBIL TransUnion score (300–900)
 * @param empType   employment classification ('SALARIED', 'SEP', 'FREELANCE', etc.)
 * @param monthlyIncome gross monthly income in ₹ — used for future FOIR gating
 */
public record ApplicantProfile(
        Integer cibil,
        String empType,
        BigDecimal monthlyIncome
) {
    /**
     * Compact constructor enforcing invariants.
     */
    public ApplicantProfile {
        if (!isNtc(cibil) && (cibil < 300 || cibil > 900)) {
            throw new IllegalArgumentException(
                    "CIBIL score must be in [300, 900]; received: " + cibil);
        }
    }

    public boolean isNtc() {
        return isNtc(this.cibil);
    }

    private static boolean isNtc(Integer cibil) {
        return cibil == null || cibil <= 0;
    }
}
