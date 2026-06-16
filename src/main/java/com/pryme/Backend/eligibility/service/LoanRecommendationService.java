package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.dto.ApplicantProfile;
import com.pryme.Backend.eligibility.dto.RecommendedProductDTO;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 Loan Recommendation Engine
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Pipeline:  FILTER  →  HYDRATE  →  RANK  →  RETURN
 *
 *   1. FILTER   — Remove products whose hard eligibility gates the applicant
 *                 cannot satisfy (CIBIL floor, loan amount band, loan type).
 *
 *   2. HYDRATE  — For each surviving product, resolve the applicant-specific
 *                 ROI and Processing Fee via the FinancialComputationEngine's
 *                 SpEL evaluation pipeline.
 *
 *   3. RANK     — Sort by dynamicRoi ASC, then dynamicPf ASC to surface
 *                 the cheapest-cost products first.
 *
 *   4. RETURN   — Emit an immutable list of {@link RecommendedProductDTO}.
 *
 * Thread-safety: Stateless service — safe for concurrent use. The underlying
 * FinancialComputationEngine creates per-invocation evaluation contexts.
 *
 * @since 2026-05-02
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoanRecommendationService {

    private final LoanProductRepository loanProductRepository;
    private final FinancialComputationEngine computationEngine;

    /**
     * Two-phase comparator: ROI ascending, ties broken by PF ascending.
     * Nulls are treated as MAX_VALUE (pushed to the bottom of the ranking).
     */
    private static final Comparator<RecommendedProductDTO> CHEAPEST_FIRST =
            Comparator.comparing(
                    RecommendedProductDTO::dynamicRoi,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ).thenComparing(
                    RecommendedProductDTO::dynamicPf,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the best-match loan products for the given applicant, ranked
     * by effective cost (ROI, then PF).
     *
     * @param profile         applicant dimensions (CIBIL, empType, income)
     * @param requestedAmount the loan amount the applicant is seeking (₹)
     * @param loanType        the product category to match ('HL', 'LAP', 'BL', etc.)
     * @return                ranked list of hydrated recommendations, cheapest first
     * @throws IllegalArgumentException if any required parameter is null/invalid
     */
    public List<RecommendedProductDTO> getBestMatches(ApplicantProfile profile,
                                                      BigDecimal requestedAmount,
                                                      String loanType) {
        // ── Input Validation ─────────────────────────────────────────────────
        Objects.requireNonNull(profile, "ApplicantProfile must not be null");
        Objects.requireNonNull(loanType, "loanType must not be null");
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "requestedAmount must be a positive value; received: " + requestedAmount);
        }

        String normalizedLoanType = loanType.trim().toUpperCase();

        // ── Load active products for this loan type ──────────────────────────
        List<LoanProduct> candidates = loanProductRepository
                .findByLoanTypeAndActive(normalizedLoanType, true);

        log.debug("Recommendation pipeline: loanType={} candidates={} cibil={} amount={}",
                normalizedLoanType, candidates.size(), profile.cibil(), requestedAmount);

        // ── FILTER → HYDRATE → RANK ─────────────────────────────────────────
        List<RecommendedProductDTO> results = candidates.stream()
                .filter(p -> passesHardGates(p, profile, requestedAmount))
                .map(p -> hydrate(p, profile, requestedAmount))
                .sorted(CHEAPEST_FIRST)
                .toList();

        log.info("Recommendation pipeline complete: loanType={} matched={} of {} candidates",
                normalizedLoanType, results.size(), candidates.size());

        return results;
    }

    /**
     * Overload: loads ALL active products across all loan types.
     * Useful for "show me everything I qualify for" scenarios.
     */
    public List<RecommendedProductDTO> getBestMatchesAllTypes(ApplicantProfile profile,
                                                              BigDecimal requestedAmount) {
        Objects.requireNonNull(profile, "ApplicantProfile must not be null");
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "requestedAmount must be a positive value; received: " + requestedAmount);
        }

        List<LoanProduct> allActive = loanProductRepository.findAll().stream()
                .filter(LoanProduct::isActive)
                .toList();

        return allActive.stream()
                .filter(p -> passesHardGates(p, profile, requestedAmount))
                .map(p -> hydrate(p, profile, requestedAmount))
                .sorted(CHEAPEST_FIRST)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTER STAGE — Hard Eligibility Gates
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if the product's hard gates are satisfied.
     * Any null gate is treated as "no restriction" (permissive default).
     */
    private boolean passesHardGates(LoanProduct product,
                                    ApplicantProfile profile,
                                    BigDecimal requestedAmount) {

        // Gate 1: CIBIL floor
        if (product.getMinCibil() != null && profile.cibil() < product.getMinCibil()) {
            return false;
        }

        // Gate 2: CIBIL ceiling (optional — some products cap at a max tier)
        if (product.getMaxCibil() != null && profile.cibil() > product.getMaxCibil()) {
            return false;
        }

        // Gate 3: Loan amount must be within [minLoanAmount, maxLoanAmount]
        if (product.getMinLoanAmount() != null
                && requestedAmount.compareTo(product.getMinLoanAmount()) < 0) {
            return false;
        }
        if (product.getMaxLoanAmount() != null
                && requestedAmount.compareTo(product.getMaxLoanAmount()) > 0) {
            return false;
        }

        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HYDRATE STAGE — Dynamic Pricing Resolution
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps a LoanProduct + ApplicantProfile → RecommendedProductDTO
     * by resolving the applicant-specific Processing Fee through the
     * FinancialComputationEngine and using the product's static ROI.
     */
    private RecommendedProductDTO hydrate(LoanProduct product,
                                           ApplicantProfile profile,
                                           BigDecimal requestedAmount) {

        BigDecimal dynamicRoi = computationEngine.resolveRoi(product, profile, requestedAmount);

        BigDecimal dynamicPf = computationEngine.resolveProcessingFee(
                product,
                requestedAmount,
                profile.empType()
        );

        BigDecimal dynamicLoginFee = computationEngine.resolveLoginFee(
                product,
                requestedAmount,
                profile.empType()
        );

        return new RecommendedProductDTO(
                product.getId(),
                product.getProductCode(),
                product.getLenderName(),
                product.getProductName(),
                product.getLoanType(),
                dynamicRoi,
                dynamicPf,
                dynamicLoginFee,
                product.getMinLoanAmount(),
                product.getMaxLoanAmount(),
                product.getAdminFee(),
                product.getInsuranceCharges(),
                product.getLegalTechnicalCharges(),
                product.getOtherExpense(),
                product.getStampDuties(),
                product.getPrepaymentCharges(),
                product.getForeclosureCharges()
        );
    }
}
