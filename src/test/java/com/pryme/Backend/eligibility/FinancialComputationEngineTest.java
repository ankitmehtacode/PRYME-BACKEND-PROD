package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.service.FinancialComputationEngine;
import com.pryme.Backend.loanproduct.entity.LoanProduct;

import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 FinancialComputationEngine — Unit Tests (Static Fee Resolution Only)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Tests the static processing fee resolution path after dynamic SpEL logic
 * (ROI, PF expression, LTV, FOIR) was removed in V17 migration.
 *
 * Coverage:
 *   1. Static % fallback
 *   2. No config → ZERO
 *   3. Input validation (null, zero, negative amounts)
 *
 * @since 2026-05-14
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialComputationEngine — Static Fee Resolution")
class FinancialComputationEngineTest {

    @Mock
    private ProductRoiMatrixRepository roiMatrixRepository;

    @InjectMocks
    private FinancialComputationEngine engine;

    @BeforeEach
    void bootstrap() {
        // Engine is injected by Mockito
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 1: STATIC PERCENTAGE PROCESSING FEE
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("1.1 — Static PF: processingFee × loanAmount")
    void staticPercentageFee() {
        var product = LoanProduct.builder()
                .productCode("SBI-HL-001")
                .processingFee(new BigDecimal("0.0050"))  // 0.50%
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"));

        // 5,000,000 × 0.005 = 25,000.00
        assertEquals(0, new BigDecimal("25000.00").compareTo(actual),
                "Static PF: expected ₹25,000 for ₹50L @ 0.50%");
    }

    @Test
    @DisplayName("1.2 — Static PF: 1% on ₹20L")
    void staticPercentageFeeOnePercent() {
        var product = LoanProduct.builder()
                .productCode("HDFC-HL-001")
                .processingFee(new BigDecimal("0.0100"))  // 1.00%
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("2000000"));

        // 2,000,000 × 0.01 = 20,000.00
        assertEquals(0, new BigDecimal("20000.00").compareTo(actual));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 2: NO CONFIG → ZERO
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("2.1 — No PF config at all → returns BigDecimal.ZERO")
    void noFeeConfig() {
        var product = LoanProduct.builder()
                .productCode("NOCFG-001")
                .processingFee(null)
                .build();

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"));

        assertEquals(0, BigDecimal.ZERO.compareTo(actual),
                "No PF config should return ZERO");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 3: INPUT VALIDATION
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("3.1 — Null loanAmount → IllegalArgumentException")
    void nullLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-001")
                .processingFee(new BigDecimal("0.0050"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveProcessingFee(product, null));
    }

    @Test
    @DisplayName("3.2 — Zero loanAmount → IllegalArgumentException")
    void zeroLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-002")
                .processingFee(new BigDecimal("0.0050"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveProcessingFee(product, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("3.3 — Negative loanAmount → IllegalArgumentException")
    void negativeLoanAmountThrows() {
        var product = LoanProduct.builder()
                .productCode("ERR-003")
                .processingFee(new BigDecimal("0.0050"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.resolveProcessingFee(product, new BigDecimal("-100000")));
    }
}
