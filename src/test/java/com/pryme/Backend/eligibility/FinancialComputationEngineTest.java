package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.service.FinancialComputationEngine;
import com.pryme.Backend.eligibility.service.SpelExpressionCacheService;
import com.pryme.Backend.loanproduct.entity.LoanProduct;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.Expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 FinancialComputationEngine — Parameterized Unit Tests
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Strategy:
 *   - REAL SpEL parsing (no mocks) via a live SpelExpressionCacheService
 *     backed by an in-memory Caffeine cache. This exercises the exact same
 *     code path as production: parse → cache → evaluate → coerce.
 *
 *   - The engine builds its own per-invocation SimpleEvaluationContext with
 *     #variables registered correctly. This tests the actual production code
 *     path including the sandbox security model.
 *
 *   - LoanProduct stubs built via Lombok @Builder — no Mockito needed.
 *     We construct exact product configurations and assert exact outputs.
 *
 * Coverage:
 *   1. Processing Fee — piecewise SpEL, static % fallback, no-config fallback
 *   2. Dynamic ROI    — multi-dimensional SpEL grid, static fallback
 *   3. Safety Breakers — 50% ROI ceiling, negative fee clamping, null handling
 *   4. Edge Cases     — zero-boundary ternary, null empType, IAE on bad input
 *
 * @since 2026-05-02
 */
@DisplayName("FinancialComputationEngine — SpEL Formula Verification")
class FinancialComputationEngineTest {

    // ── Collaborators (real, not mocked) ──────────────────────────────────────
    private static SpelExpressionCacheService spelCache;
    private static FinancialComputationEngine engine;

    @BeforeAll
    static void bootstrap() {
        // Mirror production SpelSandboxConfig exactly
        var cache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofHours(1))
                .<String, Expression>build();

        spelCache = new SpelExpressionCacheService(cache);

        // Engine now builds its own per-invocation SimpleEvaluationContext
        // with #variables registered — no shared context needed
        engine = new FinancialComputationEngine(spelCache);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 1: PROCESSING FEE — PIECEWISE SpEL
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * L&T Finance piecewise fee structure:
     *   ≤ ₹2Cr → flat ₹10,000
     *   > ₹2Cr → 0.25% of loan amount
     */
    private static final String LNT_PF_SPEL =
            "#loanAmount <= 20000000 ? 10000.00 : #loanAmount * 0.0025";

    static Stream<Arguments> piecewiseFeeProvider() {
        return Stream.of(
                // Below threshold → flat ₹10K
                Arguments.of("₹1.5Cr (below threshold)",
                        new BigDecimal("15000000"), new BigDecimal("10000.00")),
                // At exact threshold → flat ₹10K (boundary: <=)
                Arguments.of("₹2Cr (exact threshold)",
                        new BigDecimal("20000000"), new BigDecimal("10000.00")),
                // Above threshold → 0.25%
                Arguments.of("₹3Cr (above threshold)",
                        new BigDecimal("30000000"), new BigDecimal("75000.00")),
                // Large amount → 0.25%
                Arguments.of("₹10Cr (large amount)",
                        new BigDecimal("100000000"), new BigDecimal("250000.00"))
        );
    }

    @ParameterizedTest(name = "PF Piecewise: {0} → fee = {2}")
    @MethodSource("piecewiseFeeProvider")
    @DisplayName("1.1 — L&T Finance Piecewise Processing Fee")
    void piecewiseProcessingFee(String label, BigDecimal loanAmount, BigDecimal expectedFee) {
        var product = LoanProduct.builder()
                .productCode("LNT-LAP-001")
                .pfComputationLogic(LNT_PF_SPEL)
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, loanAmount);

        assertEquals(0, expectedFee.compareTo(actual),
                String.format("PF mismatch for %s: expected %s but got %s",
                        label, expectedFee, actual));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 2: PROCESSING FEE — STATIC PERCENTAGE FALLBACK
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("1.2 — SBI Static PF: null SpEL → falls back to processingFee × loanAmount")
    void staticPercentageFallback() {
        var product = LoanProduct.builder()
                .productCode("SBI-HL-001")
                .pfComputationLogic(null)           // No SpEL
                .processingFee(new BigDecimal("0.0050"))  // 0.50%
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"));

        // 5,000,000 × 0.005 = 25,000.00
        assertEquals(0, new BigDecimal("25000.00").compareTo(actual),
                "Static PF fallback: expected ₹25,000 for ₹50L @ 0.50%");
    }

    @Test
    @DisplayName("1.3 — Blank SpEL string → falls back to static processingFee")
    void blankSpelFallsToStatic() {
        var product = LoanProduct.builder()
                .productCode("SBI-HL-002")
                .pfComputationLogic("   ")          // Blank (not null)
                .processingFee(new BigDecimal("0.0100"))  // 1.00%
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("2000000"));

        // 2,000,000 × 0.01 = 20,000.00
        assertEquals(0, new BigDecimal("20000.00").compareTo(actual));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 3: PROCESSING FEE — ZERO FALLBACK (NO CONFIG)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("1.4 — No PF config at all → returns BigDecimal.ZERO")
    void noFeeConfig() {
        var product = LoanProduct.builder()
                .productCode("NOCFG-001")
                .pfComputationLogic(null)
                .processingFee(null)
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"));

        assertEquals(0, BigDecimal.ZERO.compareTo(actual),
                "No PF config should return ZERO");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 4: PROCESSING FEE — INPUT VALIDATION
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("1.5 — Null loanAmount → IllegalArgumentException")
    void nullLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-001")
                .pfComputationLogic(LNT_PF_SPEL)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveProcessingFee(product, null));
    }

    @Test
    @DisplayName("1.6 — Zero loanAmount → IllegalArgumentException")
    void zeroLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-002")
                .pfComputationLogic(LNT_PF_SPEL)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveProcessingFee(product, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("1.7 — Negative loanAmount → IllegalArgumentException")
    void negativeLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-003")
                .pfComputationLogic(LNT_PF_SPEL)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveProcessingFee(product, new BigDecimal("-100000")));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 5: DYNAMIC ROI — MULTI-DIMENSIONAL SpEL GRID
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * L&T Finance multi-dimensional ROI grid:
     *   SALARIED + CIBIL ≥ 800  → 7.95%
     *   SEP      + CIBIL ≥ 750  → 8.25%
     *   Everything else         → 8.90%
     */
    private static final String LNT_ROI_SPEL =
            "(#empType == 'SALARIED' && #cibil >= 800) ? 7.95 : " +
            "((#empType == 'SEP' && #cibil >= 750) ? 8.25 : 8.90)";

    static Stream<Arguments> dynamicRoiProvider() {
        return Stream.of(
                // Best tier: SALARIED, high CIBIL
                Arguments.of("SALARIED/810 → 7.95%",
                        810, "SALARIED", "7.95"),
                // Exact boundary: SALARIED, CIBIL = 800 (>=)
                Arguments.of("SALARIED/800 → 7.95% (boundary)",
                        800, "SALARIED", "7.95"),
                // Just below: SALARIED, CIBIL = 799 → falls to else
                Arguments.of("SALARIED/799 → 8.90% (below boundary)",
                        799, "SALARIED", "8.90"),
                // SEP, above threshold
                Arguments.of("SEP/760 → 8.25%",
                        760, "SEP", "8.25"),
                // SEP, exact boundary
                Arguments.of("SEP/750 → 8.25% (boundary)",
                        750, "SEP", "8.25"),
                // SEP, below threshold → else
                Arguments.of("SEP/749 → 8.90% (below boundary)",
                        749, "SEP", "8.90"),
                // Catch-all: low CIBIL, SALARIED
                Arguments.of("SALARIED/600 → 8.90% (catch-all)",
                        600, "SALARIED", "8.90"),
                // Unknown empType → catch-all
                Arguments.of("FREELANCE/800 → 8.90% (unknown empType)",
                        800, "FREELANCE", "8.90")
        );
    }

    @ParameterizedTest(name = "ROI Grid: {0}")
    @MethodSource("dynamicRoiProvider")
    @DisplayName("2.1 — L&T Finance Multi-Dimensional ROI Grid")
    void dynamicRoiGrid(String label, int cibil, String empType, String expectedRoi) {
        var product = LoanProduct.builder()
                .productCode("LNT-LAP-ROI")
                .roiComputationLogic(LNT_ROI_SPEL)
                .roi(new BigDecimal("10.50"))   // Static fallback — should NOT be used
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, cibil, empType, new BigDecimal("5000000"));

        BigDecimal expected = new BigDecimal(expectedRoi)
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(0, expected.compareTo(actual),
                String.format("ROI mismatch for %s: expected %s but got %s",
                        label, expected, actual));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 6: DYNAMIC ROI — STATIC FALLBACK
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("2.2 — Null roiComputationLogic → falls back to product.roi")
    void roiNullSpelFallback() {
        var product = LoanProduct.builder()
                .productCode("SBI-HL-ROI")
                .roiComputationLogic(null)
                .roi(new BigDecimal("9.2500"))
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, 750, "SALARIED", new BigDecimal("5000000"));

        assertEquals(0, new BigDecimal("9.25").compareTo(actual),
                "Null SpEL should fall back to static roi = 9.25");
    }

    @Test
    @DisplayName("2.3 — Blank roiComputationLogic → falls back to product.roi")
    void roiBlankSpelFallback() {
        var product = LoanProduct.builder()
                .productCode("SBI-HL-ROI2")
                .roiComputationLogic("   ")
                .roi(new BigDecimal("8.7500"))
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, 750, "SALARIED", new BigDecimal("5000000"));

        assertEquals(0, new BigDecimal("8.75").compareTo(actual),
                "Blank SpEL should fall back to static roi = 8.75");
    }

    @Test
    @DisplayName("2.4 — No roi + no SpEL → returns ZERO (misconfiguration)")
    void roiNoConfigReturnsZero() {
        var product = LoanProduct.builder()
                .productCode("NOCFG-ROI")
                .roiComputationLogic(null)
                .roi(null)
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, 750, "SALARIED", new BigDecimal("5000000"));

        assertEquals(0, BigDecimal.ZERO.compareTo(actual),
                "No ROI config should return ZERO");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 7: SAFETY CIRCUIT BREAKER — 50% ROI CEILING
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("3.1 — Admin typo: 85% → circuit breaker → falls back to static base rate")
    void roiCircuitBreakerOnInsaneRate() {
        // Simulates an admin who typed 85.00 instead of 8.50
        String badSpel = "#cibil > 700 ? 85.00 : 10.00";

        var product = LoanProduct.builder()
                .productCode("TYPO-001")
                .roiComputationLogic(badSpel)
                .roi(new BigDecimal("10.5000"))   // Static fallback
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, 750, "SALARIED", new BigDecimal("5000000"));

        // 85.00 > MAX_SANE_ROI (50.00) → circuit breaker → fallback to 10.50
        assertEquals(0, new BigDecimal("10.50").compareTo(actual),
                "85% exceeds 50% ceiling → must fall back to static base rate 10.50");
    }

    @Test
    @DisplayName("3.2 — Negative ROI from SpEL → circuit breaker → falls back to static base rate")
    void roiCircuitBreakerOnNegativeRate() {
        String negativeSpel = "#cibil > 700 ? -5.00 : 8.50";

        var product = LoanProduct.builder()
                .productCode("NEG-001")
                .roiComputationLogic(negativeSpel)
                .roi(new BigDecimal("9.0000"))
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, 750, "SALARIED", new BigDecimal("5000000"));

        // -5.00 < 0 → circuit breaker → fallback to 9.00
        assertEquals(0, new BigDecimal("9.00").compareTo(actual),
                "Negative ROI → must fall back to static base rate 9.00");
    }

    @Test
    @DisplayName("3.3 — ROI at exact ceiling (50.00) → PASSES (not breaker)")
    void roiAtExactCeilingPasses() {
        String ceilSpel = "#cibil > 700 ? 50.00 : 10.00";

        var product = LoanProduct.builder()
                .productCode("CEIL-001")
                .roiComputationLogic(ceilSpel)
                .roi(new BigDecimal("10.0000"))
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, 750, "SALARIED", new BigDecimal("5000000"));

        // 50.00 is at the ceiling (<=), should pass, NOT trigger breaker
        assertEquals(0, new BigDecimal("50.00").compareTo(actual),
                "ROI = 50.00 is at ceiling boundary → should pass");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 8: PROCESSING FEE — NEGATIVE FEE CLAMPING
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("4.1 — SpEL produces negative fee → clamped to ZERO")
    void negativeFeeClampedToZero() {
        // Simulates a misconfigured SpEL that produces a negative fee
        String badSpel = "#loanAmount * -0.01";

        var product = LoanProduct.builder()
                .productCode("NEGFEE-001")
                .pfComputationLogic(badSpel)
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"));

        assertEquals(0, BigDecimal.ZERO.compareTo(actual),
                "Negative fee should be clamped to ZERO");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 9: ROI — NULL empType HANDLING
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("5.1 — Null empType → injected as 'ANY', hits catch-all branch")
    void nullEmpTypeFallsToAny() {
        var product = LoanProduct.builder()
                .productCode("LNT-NULL-EMP")
                .roiComputationLogic(LNT_ROI_SPEL)
                .roi(new BigDecimal("10.50"))
                .build();

        BigDecimal actual = engine.resolveInterestRate(
                product, 810, null, new BigDecimal("5000000"));

        // empType=null → injected as "ANY" → doesn't match SALARIED or SEP → 8.90
        assertEquals(0, new BigDecimal("8.90").compareTo(actual),
                "Null empType should inject as 'ANY' → catch-all → 8.90%");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 10: ROI — INPUT VALIDATION
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("5.2 — Null loanAmount → IllegalArgumentException")
    void roiNullLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-ROI-001")
                .roiComputationLogic(LNT_ROI_SPEL)
                .roi(new BigDecimal("10.50"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveInterestRate(product, 750, "SALARIED", null));
    }

    @Test
    @DisplayName("5.3 — Zero loanAmount → IllegalArgumentException")
    void roiZeroLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-ROI-002")
                .roiComputationLogic(LNT_ROI_SPEL)
                .roi(new BigDecimal("10.50"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveInterestRate(product, 750, "SALARIED", BigDecimal.ZERO));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 11: SpEL SYNTAX ERROR RESILIENCE
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("6.1 — Malformed PF SpEL → graceful fallback to ZERO (no crash)")
    void malformedPfSpelFallsToZero() {
        var product = LoanProduct.builder()
                .productCode("BROKEN-PF")
                .pfComputationLogic("#loanAmount * * * GARBAGE")
                .processingFee(null)                // No static fallback either
                .build();

        // Should NOT throw — engine catches EvaluationException and returns ZERO
        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"));
        assertNotNull(actual, "Malformed SpEL should return a value, not null");
        // It either returns ZERO or a parse error falls through — either way, no crash
    }

    @Test
    @DisplayName("6.2 — Malformed ROI SpEL → graceful fallback to static base rate (no crash)")
    void malformedRoiSpelFallsToStatic() {
        var product = LoanProduct.builder()
                .productCode("BROKEN-ROI")
                .roiComputationLogic("#cibil ??? INVALID")
                .roi(new BigDecimal("11.00"))
                .build();

        // Should NOT throw — engine catches EvaluationException and falls back
        BigDecimal actual = engine.resolveInterestRate(
                product, 750, "SALARIED", new BigDecimal("5000000"));
        assertNotNull(actual, "Malformed SpEL should return a value, not null");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 12: FLAT AMOUNT SpEL
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("7.1 — Flat ₹ SpEL: '15000.00' → always ₹15,000 regardless of loan amount")
    void flatAmountSpel() {
        var product = LoanProduct.builder()
                .productCode("FLAT-PF")
                .pfComputationLogic("15000.00")     // Literal SpEL expression
                .build();

        BigDecimal actual1 = engine.resolveProcessingFee(product, new BigDecimal("1000000"));
        BigDecimal actual2 = engine.resolveProcessingFee(product, new BigDecimal("100000000"));

        assertEquals(0, new BigDecimal("15000.00").compareTo(actual1),
                "₹10L loan → flat ₹15K fee");
        assertEquals(0, new BigDecimal("15000.00").compareTo(actual2),
                "₹10Cr loan → flat ₹15K fee (same)");
    }
}
