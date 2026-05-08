package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 FINANCIAL COMPUTATION ENGINE — DATA-DRIVEN FEE RESOLVER
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Resolves processing fees (and future financial expressions) at runtime via SpEL
 * evaluation against per-product expressions stored in the database.
 *
 * Architecture:
 *   LoanProduct.pfComputationLogic (VARCHAR 500)
 *       → SpelExpressionCacheService (Caffeine L1 cache)
 *           → SimpleEvaluationContext (read-only sandbox, no java.lang.Runtime)
 *               → Number result → BigDecimal coercion (scale=2, HALF_UP)
 *
 * Why double injection instead of BigDecimal:
 *   SpEL's arithmetic operators (*, /, <=, ternary) operate natively on doubles.
 *   Feeding BigDecimal forces SpEL into reflective .multiply() calls which break
 *   the human-readable expression syntax admins write in the CMS. We inject the
 *   loan amount as a double, let SpEL do its math, then coerce the result back
 *   to BigDecimal with banker's rounding. The precision loss on a ₹100Cr amount
 *   at scale=2 is < 1 paisa — financially negligible.
 *
 * Fallback chain:
 *   1. pfComputationLogic (SpEL)  → exact computed fee
 *   2. processingFee (static %)   → loanAmount × staticRate
 *   3. BigDecimal.ZERO            → no fee
 *
 * @since 2026-04-30
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialComputationEngine {

    private final SpelExpressionCacheService spelCache;

    /** Scale for all INR fee outputs. */
    private static final int FEE_SCALE = 2;

    /** Scale for ROI outputs (e.g. 8.50%). */
    private static final int ROI_SCALE = 2;

    /** Sanity ceiling: no Indian lender charges > 50% p.a. */
    private static final BigDecimal MAX_SANE_ROI = new BigDecimal("50.00");

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the absolute processing fee (in ₹) for a given product and loan amount.
     *
     * @param product    the LoanProduct entity (must not be null)
     * @param loanAmount the applicant's requested loan amount (must be > 0)
     * @return           absolute processing fee as BigDecimal, scale=2, never null
     * @throws IllegalArgumentException if loanAmount is null or non-positive
     */
    public BigDecimal resolveProcessingFee(LoanProduct product, BigDecimal loanAmount) {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "loanAmount must be a positive value; received: " + loanAmount);
        }

        String spelExpression = product.getPfComputationLogic();

        // ── PATH 1: Dynamic SpEL expression ──────────────────────────────────
        if (spelExpression != null && !spelExpression.isBlank()) {
            return evaluateFeeExpression(spelExpression, loanAmount, product.getProductCode());
        }

        // ── PATH 2: Static percentage fallback ───────────────────────────────
        if (product.getProcessingFee() != null) {
            return loanAmount.multiply(product.getProcessingFee())
                    .setScale(FEE_SCALE, RoundingMode.HALF_UP);
        }

        // ── PATH 3: No fee configured ────────────────────────────────────────
        log.debug("No PF logic or static fee for product={}. Returning ZERO.",
                product.getProductCode());
        return BigDecimal.ZERO;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNALS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Evaluates a SpEL fee expression in a sandboxed context.
     *
     * Variables injected:
     *   #loanAmount — double (for native SpEL arithmetic compatibility)
     *
     * Returns: BigDecimal with scale=2, HALF_UP rounding.
     */
    private BigDecimal evaluateFeeExpression(String spelExpression,
                                             BigDecimal loanAmount,
                                             String productCode) {
        try {
            Expression expr = spelCache.getOrCompile(spelExpression);

            Map<String, Object> variables = Map.of(
                    "loanAmount", loanAmount.doubleValue()
            );

            SimpleEvaluationContext ctx = buildSandboxContext(variables);
            Number result = expr.getValue(ctx, Number.class);

            if (result == null) {
                log.error("SpEL fee expression returned null for product={} expr='{}'. " +
                          "Treating as ZERO — but this is likely a misconfiguration.",
                        productCode, spelExpression);
                return BigDecimal.ZERO;
            }

            // 🧠 COERCION: String.valueOf() avoids IEEE 754 representation artifacts
            //    that new BigDecimal(double) would introduce.
            //    Example: new BigDecimal(0.1) = 0.10000000000000000555...
            //             new BigDecimal("0.1") = 0.1  ← correct
            BigDecimal fee = new BigDecimal(String.valueOf(result.doubleValue()))
                    .setScale(FEE_SCALE, RoundingMode.HALF_UP);

            // Sanity: fees cannot be negative
            if (fee.compareTo(BigDecimal.ZERO) < 0) {
                log.error("SpEL fee expression produced negative fee={} for product={} expr='{}'. " +
                          "Clamping to ZERO.", fee, productCode, spelExpression);
                return BigDecimal.ZERO;
            }

            log.debug("Fee resolved: product={} loanAmount={} expr='{}' → fee={}",
                    productCode, loanAmount, spelExpression, fee);
            return fee;

        } catch (Exception e) {
            log.error("SpEL fee resolution FAILED for product={} expr='{}'. " +
                      "Falling back to ZERO. Error: {}",
                    productCode, spelExpression, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API — DYNAMIC ROI RESOLVER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the effective annual interest rate for a given product and applicant.
     *
     * This method evaluates the product's roiComputationLogic SpEL expression,
     * injecting applicant parameters (CIBIL, employment type, loan amount) into
     * the sandbox context. If the SpEL string is null, blank, or fails evaluation,
     * it gracefully falls back to the product's static roi field.
     *
     * @param product   the LoanProduct entity (must not be null)
     * @param cibil     the applicant's CIBIL score (300–900)
     * @param empType   the applicant's employment type ('SALARIED', 'SEP', etc.)
     * @param loanAmount the requested loan amount (must be > 0)
     * @return          effective annual ROI as BigDecimal, scale=2, never null
     */
    public BigDecimal resolveInterestRate(LoanProduct product,
                                          int cibil,
                                          String empType,
                                          BigDecimal loanAmount) {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "loanAmount must be a positive value; received: " + loanAmount);
        }

        String spelExpression = product.getRoiComputationLogic();

        // ── PATH 1: Dynamic SpEL expression ──────────────────────────────────
        if (spelExpression != null && !spelExpression.isBlank()) {
            return evaluateRoiExpression(spelExpression, cibil, empType, loanAmount,
                    product.getProductCode(), product.getRoi());
        }

        // ── PATH 2: Static base rate fallback ────────────────────────────────
        if (product.getRoi() != null) {
            log.debug("No ROI SpEL for product={}. Using static roi={}",
                    product.getProductCode(), product.getRoi());
            return product.getRoi().setScale(ROI_SCALE, RoundingMode.HALF_UP);
        }

        // ── PATH 3: No rate configured — this is a misconfiguration ──────────
        log.error("No ROI logic or static rate for product={}. Returning ZERO.",
                product.getProductCode());
        return BigDecimal.ZERO;
    }

    /**
     * Evaluates a SpEL ROI expression in a sandboxed context.
     *
     * Variables injected:
     *   #cibil      — Integer (applicant CIBIL score)
     *   #loanAmount — Double  (for native SpEL arithmetic)
     *   #empType    — String  ('SALARIED', 'SEP', etc.)
     *
     * Returns: BigDecimal (annual rate, e.g. 8.50), scale=2, HALF_UP.
     *          Falls back to static baseRate on ANY failure.
     */
    private BigDecimal evaluateRoiExpression(String spelExpression,
                                             int cibil,
                                             String empType,
                                             BigDecimal loanAmount,
                                             String productCode,
                                             BigDecimal staticBaseRate) {
        try {
            Expression expr = spelCache.getOrCompile(spelExpression);

            Map<String, Object> variables = Map.of(
                    "cibil", cibil,
                    "loanAmount", loanAmount.doubleValue(),
                    "empType", empType != null ? empType : "ANY"
            );

            SimpleEvaluationContext ctx = buildSandboxContext(variables);
            Number result = expr.getValue(ctx, Number.class);

            if (result == null) {
                log.error("SpEL ROI expression returned null for product={} expr='{}'. " +
                          "Falling back to static baseRate={}.",
                        productCode, spelExpression, staticBaseRate);
                return safeFallbackRate(staticBaseRate);
            }

            BigDecimal roi = new BigDecimal(String.valueOf(result.doubleValue()))
                    .setScale(ROI_SCALE, RoundingMode.HALF_UP);

            // Sanity: ROI should be between 0 and 50% (industry ceiling)
            if (roi.compareTo(BigDecimal.ZERO) < 0 || roi.compareTo(MAX_SANE_ROI) > 0) {
                log.error("SpEL ROI expression produced insane rate={} for product={} expr='{}'. " +
                          "Falling back to static baseRate={}.",
                        roi, productCode, spelExpression, staticBaseRate);
                return safeFallbackRate(staticBaseRate);
            }

            log.debug("ROI resolved: product={} cibil={} empType={} loanAmount={} expr='{}' → roi={}",
                    productCode, cibil, empType, loanAmount, spelExpression, roi);
            return roi;

        } catch (Exception e) {
            log.error("SpEL ROI resolution FAILED for product={} expr='{}'. " +
                      "Falling back to static baseRate={}. Error: {}",
                    productCode, spelExpression, staticBaseRate, e.getMessage());
            return safeFallbackRate(staticBaseRate);
        }
    }

    /** Safe fallback: returns the static rate or ZERO if it's null. */
    private BigDecimal safeFallbackRate(BigDecimal staticRate) {
        return staticRate != null
                ? staticRate.setScale(ROI_SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    /**
     * Resolves the dynamic LTV for an eligibility condition.
     * Evaluates SpEL (if present) -> divides by 100 (since UI sets 85 for 85%).
     * Falls back to condition.ltvAllowed or product.ltv.
     */
    public BigDecimal resolveLtv(com.pryme.Backend.eligibility.entity.EligibilityCondition condition,
                                 int cibil,
                                 String empType,
                                 BigDecimal loanAmount,
                                 BigDecimal productLtv) {
        String spel = condition.getLtvComputationLogic();
        if (spel != null && !spel.isBlank()) {
            return evaluateRatioExpression(spel, cibil, empType, loanAmount, "LTV", condition.getId(), condition.getLtvAllowed() != null ? condition.getLtvAllowed() : productLtv);
        }
        if (condition.getLtvAllowed() != null) return condition.getLtvAllowed();
        return productLtv;
    }

    /**
     * Resolves the dynamic FOIR for an eligibility condition.
     * Evaluates SpEL (if present) -> divides by 100.
     * Falls back to condition.foirMax or product.maxEmiNmiRatio.
     */
    public BigDecimal resolveFoir(com.pryme.Backend.eligibility.entity.EligibilityCondition condition,
                                  int cibil,
                                  String empType,
                                  BigDecimal loanAmount,
                                  BigDecimal productFoir) {
        String spel = condition.getFoirComputationLogic();
        if (spel != null && !spel.isBlank()) {
            return evaluateRatioExpression(spel, cibil, empType, loanAmount, "FOIR", condition.getId(), condition.getFoirMax() != null ? condition.getFoirMax() : productFoir);
        }
        if (condition.getFoirMax() != null) return condition.getFoirMax();
        return productFoir != null ? productFoir : new BigDecimal("0.65");
    }

    private BigDecimal evaluateRatioExpression(String spelExpression,
                                               int cibil,
                                               String empType,
                                               BigDecimal loanAmount,
                                               String metric,
                                               Long conditionId,
                                               BigDecimal fallback) {
        try {
            Expression expr = spelCache.getOrCompile(spelExpression);
            Map<String, Object> variables = Map.of(
                    "cibil", cibil,
                    "loanAmount", loanAmount.doubleValue(),
                    "empType", empType != null ? empType : "ANY"
            );
            SimpleEvaluationContext ctx = buildSandboxContext(variables);
            Number result = expr.getValue(ctx, Number.class);

            if (result == null) {
                log.error("SpEL {} returned null for condition={} expr='{}'. Falling back.", metric, conditionId, spelExpression);
                return fallback != null ? fallback : BigDecimal.ZERO;
            }

            // UI builder uses percentage (e.g. 85), but backend stores decimal (0.85).
            // So we divide the evaluated result by 100.
            BigDecimal ratio = new BigDecimal(String.valueOf(result.doubleValue()))
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

            log.debug("{} resolved dynamically: condition={} cibil={} empType={} expr='{}' -> {}", metric, conditionId, cibil, empType, spelExpression, ratio);
            return ratio;
        } catch (Exception e) {
            log.error("SpEL {} resolution FAILED for condition={} expr='{}'. Error: {}", metric, conditionId, spelExpression, e.getMessage());
            return fallback != null ? fallback : BigDecimal.ZERO;
        }
    }

    /**
     * Builds a per-invocation sandboxed evaluation context with #variables registered.
     *
     * Why per-invocation:
     *   SimpleEvaluationContext is NOT thread-safe for variable mutation.
     *   The builder is lightweight (~2μs) and avoids race conditions.
     *
     * Security model:
     *   - Read-only data binding (no type references, no static methods)
     *   - Instance methods allowed (for String.equals etc.)
     *   - No java.lang.Runtime, no reflection, no object construction
     */
    private SimpleEvaluationContext buildSandboxContext(Map<String, Object> variables) {
        SimpleEvaluationContext.Builder builder = SimpleEvaluationContext
                .forReadOnlyDataBinding()
                .withInstanceMethods();
        SimpleEvaluationContext ctx = builder.build();
        variables.forEach(ctx::setVariable);
        return ctx;
    }
}

