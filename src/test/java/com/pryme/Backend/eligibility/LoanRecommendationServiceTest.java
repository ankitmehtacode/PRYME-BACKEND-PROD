package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.dto.ApplicantProfile;
import com.pryme.Backend.eligibility.dto.RecommendedProductDTO;
import com.pryme.Backend.eligibility.service.FinancialComputationEngine;
import com.pryme.Backend.eligibility.service.LoanRecommendationService;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoanRecommendationService.
 * Uses Mockito for the repository and FinancialComputationEngine
 * to isolate the FILTER → HYDRATE → RANK pipeline logic.
 *
 * ROI is now read directly from product.getRoi() (static field).
 * Only Processing Fee uses the FinancialComputationEngine.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanRecommendationService — Best Match Pipeline")
class LoanRecommendationServiceTest {

    @Mock
    private LoanProductRepository loanProductRepository;
    @Mock
    private FinancialComputationEngine computationEngine;

    @InjectMocks
    private LoanRecommendationService service;

    // ── Test fixtures ────────────────────────────────────────────────────────
    private static final ApplicantProfile HIGH_CIBIL_SALARIED =
            new ApplicantProfile(810, "SALARIED", new BigDecimal("150000"));

    private static final ApplicantProfile LOW_CIBIL_SEP =
            new ApplicantProfile(660, "SEP", new BigDecimal("80000"));

    private LoanProduct sbiHl;       // CIBIL 650+, ₹10L–₹5Cr
    private LoanProduct lntLap;      // CIBIL 700+, ₹25L–₹10Cr
    private LoanProduct hdfcPrime;   // CIBIL 780+, ₹50L–₹20Cr

    @BeforeEach
    void seedProducts() {
        sbiHl = LoanProduct.builder()
                .id(1L)
                .productCode("SBI-HL-001")
                .productName("SBI Home Loan Standard")
                .loanType("HL")
                .lenderId(100L)
                .lenderName("State Bank of India")
                .interestType("FLOATING")
                .minCibil(650).maxCibil(900)
                .roi(new BigDecimal("9.2500"))

                .minTenureMonths(12).maxTenureMonths(360)
                .minLoanAmount(new BigDecimal("1000000"))
                .maxLoanAmount(new BigDecimal("50000000"))
                .active(true)
                .build();

        lntLap = LoanProduct.builder()
                .id(2L)
                .productCode("LNT-LAP-001")
                .productName("L&T LAP Premium")
                .loanType("HL")
                .lenderId(200L)
                .lenderName("L&T Finance")
                .interestType("FLOATING")
                .minCibil(700).maxCibil(900)
                .roi(new BigDecimal("8.5000"))

                .minTenureMonths(12).maxTenureMonths(240)
                .minLoanAmount(new BigDecimal("2500000"))
                .maxLoanAmount(new BigDecimal("100000000"))
                .active(true)
                .build();

        hdfcPrime = LoanProduct.builder()
                .id(3L)
                .productCode("HDFC-HL-PRIME")
                .productName("HDFC Home Prime")
                .loanType("HL")
                .lenderId(300L)
                .lenderName("HDFC Bank")
                .interestType("FLOATING")
                .minCibil(780).maxCibil(900)
                .roi(new BigDecimal("7.9500"))

                .minTenureMonths(12).maxTenureMonths(360)
                .minLoanAmount(new BigDecimal("5000000"))
                .maxLoanAmount(new BigDecimal("200000000"))
                .active(true)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 1: FILTER — CIBIL GATING
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("1.1 — Low CIBIL (620) filters out CIBIL 700+ and 780+ products")
    void lowCibilFiltersHighTierProducts() {
        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl, lntLap, hdfcPrime));
        stubProcessingFee();

        List<RecommendedProductDTO> results = service.getBestMatches(
                LOW_CIBIL_SEP, new BigDecimal("3000000"), "HL");

        assertEquals(1, results.size(), "Only SBI should pass CIBIL gate");
        assertEquals("SBI-HL-001", results.get(0).productCode());
    }

    @Test
    @DisplayName("1.2 — High CIBIL (810) qualifies for all three products")
    void highCibilPassesAllGates() {
        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl, lntLap, hdfcPrime));
        stubProcessingFee();

        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, new BigDecimal("10000000"), "HL");

        assertEquals(3, results.size(), "All 3 products should pass CIBIL gate");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 2: FILTER — LOAN AMOUNT BAND
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("2.1 — ₹40L filters out HDFC (min ₹50L)")
    void amountBelowMinFiltersProduct() {
        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl, lntLap, hdfcPrime));
        stubProcessingFee();

        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, new BigDecimal("4000000"), "HL");

        // HDFC min is ₹50L, so ₹40L should filter it out
        assertTrue(results.stream().noneMatch(r -> r.productCode().equals("HDFC-HL-PRIME")),
                "HDFC should be filtered — ₹40L < min ₹50L");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("2.2 — ₹60Cr exceeds all max caps → empty result")
    void amountAboveMaxFiltersProduct() {
        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl, lntLap, hdfcPrime));

        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, new BigDecimal("600000000"), "HL");

        // SBI max ₹5Cr, LNT max ₹10Cr, HDFC max ₹20Cr → all fail for ₹60Cr
        assertEquals(0, results.size(), "₹60Cr exceeds all maxLoanAmount caps");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 3: RANK — ROI ASCENDING, PF TIEBREAKER
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("3.1 — Products ranked by static ROI ascending (cheapest first)")
    void rankedByRoiAscending() {
        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl, lntLap, hdfcPrime));

        BigDecimal amount = new BigDecimal("10000000");
        stubProcessingFee();

        // Products have static ROIs: HDFC=7.95, LNT=8.50, SBI=9.25
        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, amount, "HL");

        assertEquals(3, results.size());
        assertEquals("HDFC-HL-PRIME", results.get(0).productCode(), "Cheapest ROI first");
        assertEquals("LNT-LAP-001", results.get(1).productCode(), "Mid ROI second");
        assertEquals("SBI-HL-001", results.get(2).productCode(), "Highest ROI last");
    }

    @Test
    @DisplayName("3.2 — Equal ROI → ties broken by PF ascending")
    void equalRoiTieBrokenByPf() {
        // Set both products to the same ROI
        sbiHl = LoanProduct.builder()
                .id(1L).productCode("SBI-HL-001").productName("SBI Home Loan Standard")
                .loanType("HL").lenderId(100L).lenderName("State Bank of India")
                .interestType("FLOATING").minCibil(650).maxCibil(900)
                .roi(new BigDecimal("8.5000"))

                .minTenureMonths(12).maxTenureMonths(360)
                .minLoanAmount(new BigDecimal("1000000"))
                .maxLoanAmount(new BigDecimal("50000000"))
                .active(true).build();

        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl, lntLap));

        BigDecimal amount = new BigDecimal("10000000");

        // Different PF: SBI=₹25K, LNT=₹10K → LNT should rank first
        when(computationEngine.resolveProcessingFee(eq(sbiHl), eq(amount), anyString()))
                .thenReturn(new BigDecimal("25000.00"));
        when(computationEngine.resolveProcessingFee(eq(lntLap), eq(amount), anyString()))
                .thenReturn(new BigDecimal("10000.00"));
        when(computationEngine.resolveLoginFee(any(), any(), any()))
                .thenReturn(new BigDecimal("1000.00"));
        when(computationEngine.resolveRoi(any(), any(), any()))
                .thenAnswer(inv -> {
                    LoanProduct p = inv.getArgument(0);
                    return p.getRoi();
                });

        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, amount, "HL");

        assertEquals(2, results.size());
        assertEquals("LNT-LAP-001", results.get(0).productCode(), "Lower PF wins tie");
        assertEquals("SBI-HL-001", results.get(1).productCode());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 4: HYDRATE — DTO FIELD INTEGRITY
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("4.1 — RecommendedProductDTO carries all expected fields")
    void dtoFieldIntegrity() {
        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl));

        BigDecimal amount = new BigDecimal("3000000");

        when(computationEngine.resolveProcessingFee(eq(sbiHl), eq(amount), anyString()))
                .thenReturn(new BigDecimal("15000.00"));
        when(computationEngine.resolveLoginFee(eq(sbiHl), eq(amount), anyString()))
                .thenReturn(new BigDecimal("1000.00"));
        when(computationEngine.resolveRoi(any(), any(), any()))
                .thenAnswer(inv -> {
                    LoanProduct p = inv.getArgument(0);
                    return p.getRoi();
                });

        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, amount, "HL");

        assertEquals(1, results.size());
        RecommendedProductDTO dto = results.get(0);

        assertAll("DTO field check",
                () -> assertEquals(1L, dto.productId()),
                () -> assertEquals("SBI-HL-001", dto.productCode()),
                () -> assertEquals("State Bank of India", dto.bankName()),
                () -> assertEquals("SBI Home Loan Standard", dto.productName()),
                () -> assertEquals("HL", dto.loanType()),
                () -> assertEquals(new BigDecimal("9.2500"), dto.dynamicRoi()),
                () -> assertEquals(new BigDecimal("15000.00"), dto.dynamicPf()),
                () -> assertEquals(new BigDecimal("1000.00"), dto.dynamicLoginFee()),
                () -> assertEquals(new BigDecimal("1000000"), dto.minLoanAmount()),
                () -> assertEquals(new BigDecimal("50000000"), dto.maxLoanAmount())
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECTION 5: EDGE CASES — INPUT VALIDATION
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("5.1 — Null profile → NullPointerException")
    void nullProfileThrows() {
        assertThrows(NullPointerException.class,
                () -> service.getBestMatches(null, new BigDecimal("5000000"), "HL"));
    }

    @Test
    @DisplayName("5.2 — Null loanType → NullPointerException")
    void nullLoanTypeThrows() {
        assertThrows(NullPointerException.class,
                () -> service.getBestMatches(HIGH_CIBIL_SALARIED, new BigDecimal("5000000"), null));
    }

    @Test
    @DisplayName("5.3 — Zero requestedAmount → IllegalArgumentException")
    void zeroAmountThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getBestMatches(HIGH_CIBIL_SALARIED, BigDecimal.ZERO, "HL"));
    }

    @Test
    @DisplayName("5.4 — No matching products → empty list, no crash")
    void noMatchesReturnsEmpty() {
        when(loanProductRepository.findByLoanTypeAndActive("BL", true))
                .thenReturn(List.of());

        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, new BigDecimal("5000000"), "BL");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("5.5 — ApplicantProfile rejects CIBIL < 300")
    void invalidCibilThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApplicantProfile(200, "SALARIED", new BigDecimal("100000")));
    }

    @Test
    @DisplayName("5.6 — Loan type is normalized to uppercase")
    void loanTypeNormalized() {
        when(loanProductRepository.findByLoanTypeAndActive("HL", true))
                .thenReturn(List.of(sbiHl));
        stubProcessingFee();

        // Pass lowercase — should be normalized internally
        List<RecommendedProductDTO> results = service.getBestMatches(
                HIGH_CIBIL_SALARIED, new BigDecimal("3000000"), " hl ");

        assertEquals(1, results.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Stubs engine with static PF for tests that don't need specific fee values. */
    private void stubProcessingFee() {
        when(computationEngine.resolveProcessingFee(any(), any(), any()))
                .thenReturn(new BigDecimal("10000.00"));
        when(computationEngine.resolveLoginFee(any(), any(), any()))
                .thenReturn(new BigDecimal("1000.00"));
        when(computationEngine.resolveRoi(any(), any(), any()))
                .thenAnswer(inv -> {
                    LoanProduct p = inv.getArgument(0);
                    return p.getRoi();
                });
    }
}
