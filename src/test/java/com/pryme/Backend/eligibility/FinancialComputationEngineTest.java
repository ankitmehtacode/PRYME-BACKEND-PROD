package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.service.FinancialComputationEngine;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.entity.ProductPfMatrix;
import com.pryme.Backend.loanproduct.entity.ProductLoginFeeMatrix;
import com.pryme.Backend.loanproduct.repository.ProductLoginFeeMatrixRepository;
import com.pryme.Backend.loanproduct.repository.ProductPfMatrixRepository;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    @Mock
    private ProductPfMatrixRepository pfMatrixRepository;
    @Mock
    private ProductLoginFeeMatrixRepository loginFeeMatrixRepository;

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

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 4: DYNAMIC PROCESSING FEE MATRIX
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("4.1 — Dynamic flat fee with tax (18%) resolution")
    void dynamicFlatFeeWithTax() {
        var product = LoanProduct.builder()
                .id(1L)
                .productCode("JIO-HL-001")
                .build();

        var matrixRow = ProductPfMatrix.builder()
                .id(101L)
                .productId(1L)
                .employmentType("Salaried")
                .minLoanAmount(new BigDecimal("2000000"))
                .maxLoanAmount(new BigDecimal("20000000"))
                .feeValue(new BigDecimal("10000.0000"))
                .flat(true)
                .taxRate(new BigDecimal("0.1800"))
                .build();

        when(pfMatrixRepository.findByProductId(1L)).thenReturn(List.of(matrixRow));

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"), "Salaried");

        // expected base = 10,000.00. Tax = 18%. Total = 11,800.00
        assertEquals(0, new BigDecimal("11800.00").compareTo(actual),
                "Dynamic flat fee matching should calculate base * (1 + taxRate)");
    }

    @Test
    @DisplayName("4.2 — Dynamic percentage fee with tax (18%) resolution")
    void dynamicPercentageFeeWithTax() {
        var product = LoanProduct.builder()
                .id(2L)
                .productCode("SBI-LAP-001")
                .build();

        var matrixRow = ProductPfMatrix.builder()
                .id(102L)
                .productId(2L)
                .employmentType("SEP_SENP")
                .minLoanAmount(new BigDecimal("2000000"))
                .maxLoanAmount(new BigDecimal("50000000"))
                .feeValue(new BigDecimal("0.0025"))  // 0.25%
                .flat(false)
                .taxRate(new BigDecimal("0.1800"))
                .build();

        when(pfMatrixRepository.findByProductId(2L)).thenReturn(List.of(matrixRow));

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("10000000"), "SEP/SENP");

        // 10,000,000 * 0.0025 = 25,000 base fee
        // 25,000 * 1.18 = 29,500.00
        assertEquals(0, new BigDecimal("29500.00").compareTo(actual),
                "Dynamic percentage fee matching should calculate (amount * rate) * (1 + taxRate)");
    }

    @Test
    @DisplayName("4.3 — Dynamic fee applies min/max fee limits before tax")
    void dynamicFeeMinMaxLimits() {
        var product = LoanProduct.builder()
                .id(3L)
                .build();

        // Row 1: max fee cap
        var matrixRowMax = ProductPfMatrix.builder()
                .id(103L)
                .productId(3L)
                .employmentType("Salaried")
                .feeValue(new BigDecimal("0.0100")) // 1%
                .flat(false)
                .minFee(new BigDecimal("5000.00"))
                .maxFee(new BigDecimal("15000.00"))
                .taxRate(new BigDecimal("0.1800"))
                .build();

        when(pfMatrixRepository.findByProductId(3L)).thenReturn(List.of(matrixRowMax));

        // Loan = 10,000,000. 1% is 100,000. Capped at 15,000. Total with tax: 15,000 * 1.18 = 17,700
        BigDecimal actualMax = engine.resolveProcessingFee(product, new BigDecimal("10000000"), "Salaried");
        assertEquals(0, new BigDecimal("17700.00").compareTo(actualMax),
                "Calculated fee should be capped at max_fee before tax application");

        // Row 2: min fee cap
        var matrixRowMin = ProductPfMatrix.builder()
                .id(104L)
                .productId(3L)
                .employmentType("Salaried")
                .feeValue(new BigDecimal("0.0010")) // 0.1%
                .flat(false)
                .minFee(new BigDecimal("5000.00"))
                .maxFee(new BigDecimal("15000.00"))
                .taxRate(new BigDecimal("0.1800"))
                .build();

        when(pfMatrixRepository.findByProductId(3L)).thenReturn(List.of(matrixRowMin));

        // Loan = 2,000,000. 0.1% is 2,000. Raised to 5,000. Total with tax: 5,000 * 1.18 = 5,900
        BigDecimal actualMin = engine.resolveProcessingFee(product, new BigDecimal("2000000"), "Salaried");
        assertEquals(0, new BigDecimal("5900.00").compareTo(actualMin),
                "Calculated fee should be raised to min_fee before tax application");
    }

    @Test
    @DisplayName("4.4 — Dynamic fee matches compound/normalized employment types")
    void dynamicFeeEmploymentTypeMatching() {
        var product = LoanProduct.builder()
                .id(4L)
                .build();

        // 1. SALARIED_SEP matches Salaried and SEP/SENP
        var rowSalariedSep = ProductPfMatrix.builder()
                .id(105L)
                .productId(4L)
                .employmentType("SALARIED_SEP")
                .feeValue(new BigDecimal("10000.00"))
                .flat(true)
                .taxRate(new BigDecimal("0.18"))
                .build();

        when(pfMatrixRepository.findByProductId(4L)).thenReturn(List.of(rowSalariedSep));

        BigDecimal feeSalaried = engine.resolveProcessingFee(product, new BigDecimal("3000000"), "Salaried");
        BigDecimal feeSep = engine.resolveProcessingFee(product, new BigDecimal("3000000"), "SEP/SENP");
        assertEquals(0, new BigDecimal("11800.00").compareTo(feeSalaried));
        assertEquals(0, new BigDecimal("11800.00").compareTo(feeSep));

        // 2. SEP_SENP, SENP, SEP match SEP/SENP
        var rowSepSenp = ProductPfMatrix.builder()
                .id(106L)
                .productId(4L)
                .employmentType("SEP_SENP")
                .feeValue(new BigDecimal("12000.00"))
                .flat(true)
                .taxRate(new BigDecimal("0.18"))
                .build();

        when(pfMatrixRepository.findByProductId(4L)).thenReturn(List.of(rowSepSenp));
        BigDecimal feeSepSenp = engine.resolveProcessingFee(product, new BigDecimal("3000000"), "SEP/SENP");
        assertEquals(0, new BigDecimal("14160.00").compareTo(feeSepSenp));
    }

    @Test
    @DisplayName("4.5 — Dynamic fee returns static fallback when no dynamic slabs match")
    void dynamicFeeFallbackToStatic() {
        var product = LoanProduct.builder()
                .id(5L)
                .processingFee(new BigDecimal("0.0050")) // 0.50% static fallback
                .build();

        // No dynamic slabs configured for this product (empty list)
        when(pfMatrixRepository.findByProductId(5L)).thenReturn(List.of());

        BigDecimal actual = engine.resolveProcessingFee(product, new BigDecimal("5000000"), "Salaried");

        // Should fall back to: 5,000,000 * 0.0050 = 25,000.00 (tax-exclusive static fallback)
        assertEquals(0, new BigDecimal("25000.00").compareTo(actual),
                "Should fall back to static fee configuration when matrix is empty");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 5: DYNAMIC LOGIN FEE RESOLUTION
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("5.1 — Dynamic login fee matching")
    void dynamicLoginFeeMatching() {
        var product = LoanProduct.builder()
                .id(10L)
                .productCode("JIO-LAP-0001")
                .build();

        var matrixRow = ProductLoginFeeMatrix.builder()
                .id(201L)
                .productId(10L)
                .employmentType("Salaried")
                .minLoanAmount(new BigDecimal("3000000"))
                .maxLoanAmount(new BigDecimal("500000000"))
                .loginFee(new BigDecimal("3250.00"))
                .build();

        when(loginFeeMatrixRepository.findByProductId(10L)).thenReturn(List.of(matrixRow));

        BigDecimal actual = engine.resolveLoginFee(product, new BigDecimal("10000000"), "Salaried");

        assertEquals(0, new BigDecimal("3250.00").compareTo(actual),
                "Dynamic login fee should match salaried slab");
    }

    @Test
    @DisplayName("5.2 — Dynamic login fee fallback to static")
    void dynamicLoginFeeFallbackToStatic() {
        var product = LoanProduct.builder()
                .id(11L)
                .loginFees(new BigDecimal("1500.00"))
                .build();

        when(loginFeeMatrixRepository.findByProductId(11L)).thenReturn(List.of());

        BigDecimal actual = engine.resolveLoginFee(product, new BigDecimal("5000000"), "Salaried");

        assertEquals(0, new BigDecimal("1500.00").compareTo(actual),
                "Should fall back to static product login fee");
    }
}
