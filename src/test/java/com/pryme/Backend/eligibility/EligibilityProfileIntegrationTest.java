package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Silicon-valley-grade integration test suite for the Pryme Eligibility Engine.
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><b>1 test = 1 profile</b> — CI shows exactly which profile broke</li>
 *   <li><b>Type-safe fixtures</b> — no markdown parsing, no regex extraction</li>
 *   <li><b>Multi-layer assertions</b> — status, product codes, ROI ordering, exclusions, rejection reasons</li>
 *   <li><b>Structured diagnostics</b> — each test prints a formatted report for quick triage</li>
 * </ul>
 *
 * <h2>Database</h2>
 * <p>Connects to the Neon production database (read-only SELECTs via {@code evaluate()}).
 * No data mutations. Redis/Bucket4j mocked out since rate-limiting is irrelevant for unit tests.</p>
 *
 * @see TestProfileFixtures  — all 18 request payloads
 * @see ProfileExpectation    — expected result value objects
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://ep-empty-boat-a1abgqec-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require",
        "spring.datasource.username=neondb_owner",
        "spring.datasource.password=npg_VbzCd0Anf8oZ",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Eligibility Engine — 18-Profile Integration Suite")
public class EligibilityProfileIntegrationTest {

    @MockBean private RedisClient redisClient;
    @MockBean private ProxyManager<byte[]> proxyManager;

    @Autowired private EligibilityEngineService engine;
    @Autowired private com.pryme.Backend.loanproduct.repository.LoanProductRepository loanProductRepository;
    @Autowired private com.pryme.Backend.eligibility.repository.EligibilityConditionRepository eligibilityConditionRepository;

    // ─── ROI TOLERANCE ───────────────────────────────────────────────────────
    // ROI is resolved dynamically via FinancialComputationEngine from DB slabs.

    @Test @Order(1)
    @DisplayName("Profile 01 — Salaried · Home Loan · NIP · Multi-Bank Aggregator")
    void profile01_salariedHomeLoanNip() {
        var expect = ProfileExpectation.eligible(1,
                "Salaried HL NIP — 11-bank coverage",
                Set.of("BOB-HL", "HDFC-HL", "SBI-HL", "ICICI-HL", "YES-HL",
                        "BANDHAN-HL", "ABFL-HL", "LT-HL", "BAJAJ-HL", "TATA-HL", "JIO-HL"),
                Set.of()
        );
        executeAndAssert(TestProfileFixtures.profile01(), expect);
    }

    @Test @Order(2)
    @DisplayName("Profile 02 — Salaried · LAP · NIP · Multi-Bank")
    void profile02_salariedLapNip() {
        var expect = ProfileExpectation.eligible(2,
                "Salaried LAP NIP — 9-bank coverage",
                Set.of("LT-LAP", "HDFC-LAP", "SBI-LAP", "ICICI-LAP", "BOB-LAP",
                        "YES-LAP", "BANDHAN-LAP", "TATA-LAP", "ICICIHFC-LAP"),
                Set.of("BAJAJ-LAP", "ABFL-LAP")
        );
        executeAndAssert(TestProfileFixtures.profile02(), expect);
    }

    @Test @Order(3)
    @DisplayName("Profile 03 — Professional · Doctor · Home Loan · SENP (2.5× multiplier)")
    void profile03_professionalDoctorSenp() {
        var expect = ProfileExpectation.eligible(3,
                "Professional Doctor HL SENP — 2.5× multiplier",
                Set.of("BOB-HL", "HDFC-HL", "SBI-HL", "ICICI-HL", "BANDHAN-HL",
                        "YES-HL", "ABFL-HL", "LT-HL", "BAJAJ-HL"),
                Set.of()
        );
        executeAndAssert(TestProfileFixtures.profile03(), expect);
    }

    @Test @Order(4)
    @DisplayName("Profile 04 — Professional · CA · LAP · SEP (L&T bank-specific multiplier)")
    void profile04_professionalCaSep() {
        var expect = ProfileExpectation.eligible(4,
                "Professional CA LAP SEP — L&T 2.5× multiplier",
                Set.of("LT-LAP", "HDFC-LAP", "SBI-LAP", "ICICI-LAP", "BOB-LAP",
                        "YES-LAP", "BANDHAN-LAP", "BAJAJ-LAP", "ABFL-LAP", "JIO-LAP"),
                Set.of()
        );
        executeAndAssert(TestProfileFixtures.profile04(), expect);
    }

    @Test @Order(5)
    @DisplayName("Profile 05 — Self-Employed · ITR/NIP · Home Loan (P&L add-backs)")
    void profile05_selfEmployedNipWithAddBacks() {
        var expect = ProfileExpectation.eligible(5,
                "Self-Employed HL NIP — PAT + Depreciation + Interest add-backs",
                Set.of("BOB-HL", "HDFC-HL", "SBI-HL", "ICICI-HL", "LT-HL",
                        "BANDHAN-HL", "YES-HL", "ABFL-HL", "BAJAJ-HL", "TATA-HL", "JIO-HL"),
                Set.of()
        );
        executeAndAssert(TestProfileFixtures.profile05(), expect);
    }

    @Test @Order(6)
    @DisplayName("Profile 06 — Self-Employed · GST · LAP (Commercial SHOP property)")
    void profile06_selfEmployedGstLap() {
        var expect = ProfileExpectation.eligible(6,
                "Self-Employed LAP GST — Retail margin, SHOP property",
                Set.of("LT-LAP", "BOB-LAP", "HDFC-LAP", "SBI-LAP", "TATA-LAP"),
                Set.of("BAJAJ-LAP", "YES-LAP", "ABFL-LAP", "ICICI-LAP", "BANDHAN-LAP")
        );
        executeAndAssert(TestProfileFixtures.profile06(), expect);
    }

    @Test @Order(7)
    @DisplayName("Profile 07 — Self-Employed · Banking · Home Loan (ABB + 453xxx pincode)")
    void profile07_selfEmployedBanking() {
        var expect = ProfileExpectation.eligible(7,
                "Self-Employed HL BANKING — ABB computation, 453xxx pincode",
                Set.of("BOB-HL", "SBI-HL", "ICICI-HL", "LT-HL", "BANDHAN-HL", "TATA-HL"),
                Set.of("BAJAJ-HL", "ABFL-HL", "YES-HL", "HDFC-HL")
        );
        executeAndAssert(TestProfileFixtures.profile07(), expect);
    }

    @Test @Order(8)
    @DisplayName("Profile 08 — Self-Employed · CashFlow · LAP (Age boundary 50yr)")
    void profile08_selfEmployedCashFlowLap() {
        var expect = ProfileExpectation.eligible(8,
                "Self-Employed LAP CashFlow — Age-at-maturity edge case",
                Set.of("BOB-LAP", "HDFC-LAP", "SBI-LAP", "ICICI-LAP", "YES-LAP",
                        "LT-LAP", "BANDHAN-LAP", "TATA-LAP", "JIO-LAP"),
                Set.of("BAJAJ-LAP", "ABFL-LAP")
        );
        executeAndAssert(TestProfileFixtures.profile08(), expect);
    }

    @Test @Order(9)
    @DisplayName("Profile 09 — Professional · CS · Home Loan · CPM_SEP (Most complex path)")
    void profile09_professionalCsCpmSep() {
        var expect = ProfileExpectation.eligible(9,
                "Professional CS HL CPM_SEP — Hybrid: PAT + Depreciation × multiplier",
                Set.of("BOB-HL", "HDFC-HL", "SBI-HL", "ICICI-HL", "BANDHAN-HL",
                        "YES-HL", "ABFL-HL", "LT-HL", "BAJAJ-HL"),
                Set.of()
        );
        executeAndAssert(TestProfileFixtures.profile09(), expect);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  REJECTION & FILTER PROFILES (10–18) — Boundary conditions
    // ═══════════════════════════════════════════════════════════════════════════

    @Test @Order(10)
    @DisplayName("Profile 10 — GEO-FENCE REJECTION · Mumbai PIN 400001")
    void profile10_geoFenceRejection() {
        var results = engine.evaluate(TestProfileFixtures.profile10());
        printDiagnostics(10, "GEO-FENCE REJECTION — Mumbai PIN", results);

        // Geo-fence rejects at pre-flight: returns a single rejected result
        assertFalse(results.isEmpty(), "Expected at least one result (the rejection)");

        boolean allIneligible = results.stream().noneMatch(EligibilityResult::isEligible);
        assertTrue(allIneligible, "Profile 10: Expected ALL results to be ineligible (geo-fence rejection)");

        // Verify rejection message mentions the pincode
        boolean hasGeoReason = results.stream()
                .flatMap(r -> r.rejectionReasons() != null ? r.rejectionReasons().stream() : java.util.stream.Stream.empty())
                .anyMatch(reason -> reason.contains("400001") || reason.toUpperCase().contains("OUTSIDE INDORE")
                        || reason.toUpperCase().contains("SERVICE AREA"));
        assertTrue(hasGeoReason, "Profile 10: Rejection reason must reference PIN 400001 or Indore geo-fence");
    }

    @Test @Order(11)
    @DisplayName("Profile 11 — Minimum Loan Amount Filter · ₹15L HL")
    void profile11_minLoanAmountFilter() {
        var expect = ProfileExpectation.eligible(11,
                "Min Loan Amount — ₹15L filters out most prime lenders",
                Set.of("SBI-HL", "BANDHAN-HL", "TATA-HL", "BOB-HL"),
                Set.of("ABFL-HL", "BAJAJ-HL", "YES-HL", "ICICI-HL")
        );
        executeAndAssert(TestProfileFixtures.profile11(), expect);
    }

    @Test @Order(12)
    @DisplayName("Profile 12 — Low CIBIL Score 660 · Home Loan")
    void profile12_lowCibilScore() {
        var expect = ProfileExpectation.eligible(12,
                "Low CIBIL 660 — Lenders with minCibil <= 650 qualify",
                Set.of("SBI-HL", "TATA-HL", "BOB-HL", "HDFC-HL", "LT-HL", "JIO-HL"),
                Set.of("ABFL-HL", "BAJAJ-HL", "YES-HL", "BANDHAN-HL", "ICICI-HL")
        );
        executeAndAssert(TestProfileFixtures.profile12(), expect);
    }

    @Test @Order(13)
    @DisplayName("Profile 13 — Age-at-Maturity · 48yr + 20yr tenure = 68yr maturity")
    void profile13_ageAtMaturity() {
        var expect = ProfileExpectation.rejected(13,
                "Age-at-maturity 68 — All banks reject (floor is 65 for salaried)",
                "AGE_TOO_HIGH"
        );
        executeAndAssert(TestProfileFixtures.profile13(), expect);
    }

    @Test @Order(14)
    @DisplayName("Profile 14 — Minimum Income Threshold · ₹28K salaried")
    void profile14_minIncomeThreshold() {
        var expect = ProfileExpectation.eligible(14,
                "Min Income ₹28K — Lenders with minIncome <= 28K qualify",
                Set.of("SBI-HL", "TATA-HL", "BANDHAN-HL", "ICICI-HL", "YES-HL", "BOB-HL", "HDFC-HL", "LT-HL"),
                Set.of("ABFL-HL", "BAJAJ-HL")
        );
        executeAndAssert(TestProfileFixtures.profile14(), expect);
    }

    @Test @Order(15)
    @DisplayName("Profile 15 — Property Type Restriction · PLOT")
    void profile15_propertyTypePlot() {
        var expect = ProfileExpectation.eligible(15,
                "PLOT property — All lenders accepting PLOT (or without restriction)",
                Set.of("YES-HL", "ABFL-HL", "BAJAJ-HL", "SBI-HL", "TATA-HL", "BANDHAN-HL", "ICICI-HL", "BOB-HL", "HDFC-HL", "LT-HL", "JIO-HL"),
                Set.of()
        );
        executeAndAssert(TestProfileFixtures.profile15(), expect);
    }

    @Test @Order(16)
    @DisplayName("Profile 16 — Combined: Low CIBIL 660 + Low Income ₹32K")
    void profile16_combinedCibilAndIncome() {
        var expect = ProfileExpectation.eligible(16,
                "Combined CIBIL 660 + Income ₹32K — Highly restricted",
                Set.of("BOB-HL", "LT-HL", "SBI-HL", "TATA-HL", "HDFC-HL"),
                Set.of("ABFL-HL", "BAJAJ-HL", "YES-HL", "BANDHAN-HL", "ICICI-HL", "JIO-HL")
        );
        executeAndAssert(TestProfileFixtures.profile16(), expect);
    }

    @Test @Order(17)
    @DisplayName("Profile 17 — SE GST Low Turnover + 2yr Vintage")
    void profile17_gstLowTurnoverShortVintage() {
        var expect = ProfileExpectation.eligible(17,
                "GST Wholesale 2yr vintage — Qualifies under NIP fallback",
                Set.of("SBI-HL", "BANDHAN-HL", "ICICI-HL", "BOB-HL", "HDFC-HL", "LT-HL", "TATA-HL", "JIO-HL"),
                Set.of("BAJAJ-HL", "ABFL-HL", "YES-HL")
        );
        executeAndAssert(TestProfileFixtures.profile17(), expect);
    }

    @Test @Order(18)
    @DisplayName("Profile 18 — LOW_LTV Surrogate Fallback · CIBIL 660 + LTV 37.5%")
    void profile18_lowLtvFallback() {
        var expect = ProfileExpectation.eligible(18,
                "LOW_LTV fallback — Lenders with minCibil <= 650 qualify",
                Set.of("SBI-HL", "TATA-HL", "BOB-HL", "HDFC-HL", "LT-HL", "JIO-HL"),
                Set.of("ABFL-HL", "BAJAJ-HL", "YES-HL", "BANDHAN-HL", "ICICI-HL")
        );
        executeAndAssert(TestProfileFixtures.profile18(), expect);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ASSERTION ENGINE — Shared validation infrastructure
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Executes the engine and runs multi-layer assertions against the expectation.
     *
     * <p>Layers:</p>
     * <ol>
     *   <li>Eligibility status (eligible vs rejected)</li>
     *   <li>Product code set — exact match</li>
     *   <li>Exclusion verification — forbidden products absent</li>
     *   <li>ROI monotonicity — results sorted ascending</li>
     *   <li>Rejection keyword check (if applicable)</li>
     * </ol>
     */
    private void executeAndAssert(EligibilityRequest request, ProfileExpectation expect) {
        List<EligibilityResult> results = engine.evaluate(request);

        // Map to public codes (BANK-PRODUCT) for comparison
        List<EligibilityResult> eligibleResults = results.stream()
                .filter(EligibilityResult::isEligible)
                .toList();

        Set<String> actualProductCodes = eligibleResults.stream()
                .map(EligibilityResult::toPublicResult)
                .map(EligibilityResult::productCode)
                .filter(code -> code != null)
                .collect(Collectors.toSet());

        // ── Diagnostic output ──
        printDiagnostics(expect.profileId(), expect.description(), results);

        // ── LAYER 1: Eligibility status ──
        if (expect.expectEligible()) {
            assertFalse(eligibleResults.isEmpty(),
                    String.format("Profile %d: Expected ELIGIBLE but got zero eligible offers. " +
                            "Rejection reasons: %s", expect.profileId(), collectRejectionReasons(results)));
        } else {
            assertTrue(eligibleResults.isEmpty(),
                    String.format("Profile %d: Expected REJECTED but got %d eligible offers: %s",
                            expect.profileId(), eligibleResults.size(), actualProductCodes));
        }

        // ── LAYER 2: Product code set — exact match ──
        if (!expect.expectedProductCodes().isEmpty()) {
            Set<String> missing = expect.expectedProductCodes().stream()
                    .filter(code -> !actualProductCodes.contains(code))
                    .collect(Collectors.toSet());
            Set<String> unexpected = actualProductCodes.stream()
                    .filter(code -> !expect.expectedProductCodes().contains(code))
                    .collect(Collectors.toSet());

            if (!missing.isEmpty() || !unexpected.isEmpty()) {
                fail(String.format(
                        "Profile %d: Product code mismatch.\n" +
                        "  Expected:   %s\n" +
                        "  Actual:     %s\n" +
                        "  Missing:    %s\n" +
                        "  Unexpected: %s\n" +
                        "  All evaluated:\n%s",
                        expect.profileId(),
                        expect.expectedProductCodes(),
                        actualProductCodes,
                        missing.isEmpty() ? "(none)" : missing,
                        unexpected.isEmpty() ? "(none)" : unexpected,
                        formatAllResults(results)
                ));
            }
        }

        // ── LAYER 3: Exclusion verification ──
        for (String excluded : expect.excludedProductCodes()) {
            assertFalse(actualProductCodes.contains(excluded),
                    String.format("Profile %d: Excluded product '%s' was present in eligible set %s",
                            expect.profileId(), excluded, actualProductCodes));
        }

        // ── LAYER 4: ROI monotonicity — eligible results must be sorted ascending ──
        if (eligibleResults.size() > 1) {
            for (int i = 1; i < eligibleResults.size(); i++) {
                BigDecimal prevRoi = eligibleResults.get(i - 1).roi();
                BigDecimal currRoi = eligibleResults.get(i).roi();
                assertTrue(currRoi.compareTo(prevRoi) >= 0,
                        String.format("Profile %d: ROI not sorted. Position %d (%s) has ROI %s, " +
                                "but position %d (%s) has ROI %s",
                                expect.profileId(),
                                i - 1, eligibleResults.get(i - 1).productCode(), prevRoi,
                                i, eligibleResults.get(i).productCode(), currRoi));
            }
        }

        // ── LAYER 5: Rejection keyword (for rejection profiles) ──
        if (expect.rejectionKeyword() != null) {
            boolean hasKeyword = results.stream()
                    .flatMap(r -> r.rejectionReasons() != null
                            ? r.rejectionReasons().stream()
                            : java.util.stream.Stream.empty())
                    .anyMatch(reason -> reason.toUpperCase().contains(expect.rejectionKeyword().toUpperCase()));
            assertTrue(hasKeyword,
                    String.format("Profile %d: Expected rejection keyword '%s' not found in reasons: %s",
                            expect.profileId(), expect.rejectionKeyword(), collectRejectionReasons(results)));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  DIAGNOSTIC OUTPUT — Structured reporting for CI and local triage
    // ═══════════════════════════════════════════════════════════════════════════

    private void printDiagnostics(int profileId, String description, List<EligibilityResult> results) {
        List<EligibilityResult> eligible = results.stream()
                .filter(EligibilityResult::isEligible)
                .toList();

        String productList = eligible.stream()
                .map(r -> {
                    String code = r.toPublicResult().productCode();
                    String roi = r.roi() != null ? r.roi().setScale(2, RoundingMode.HALF_UP) + "%" : "?%";
                    return code + " (" + roi + ")";
                })
                .collect(Collectors.joining(", "));

        String topRoi = eligible.isEmpty() ? "N/A"
                : eligible.get(0).roi().setScale(2, RoundingMode.HALF_UP) + "% ("
                  + eligible.get(0).toPublicResult().productCode() + ")";

        System.out.printf("""
                ╔══════════════════════════════════════════════════════════════════╗
                ║ PROFILE %02d — %-53s║
                ╠══════════════════════════════════════════════════════════════════╣
                ║ Status:   %-55s║
                ║ Offers:   %-4d / %-4d total evaluated                            ║
                ║ Top ROI:  %-55s║
                ║ Products: %-55s║
                ╚══════════════════════════════════════════════════════════════════╝
                """,
                profileId, description,
                eligible.isEmpty() ? "REJECTED" : "ELIGIBLE",
                eligible.size(), results.size(),
                topRoi,
                productList.isEmpty() ? "(none)" : productList
        );
    }

    private String collectRejectionReasons(List<EligibilityResult> results) {
        return results.stream()
                .filter(r -> !r.isEligible())
                .flatMap(r -> r.rejectionReasons() != null
                        ? r.rejectionReasons().stream()
                        : java.util.stream.Stream.empty())
                .limit(10) // prevent log explosion
                .collect(Collectors.joining(" | "));
    }

    private String formatAllResults(List<EligibilityResult> results) {
        StringBuilder sb = new StringBuilder();
        for (EligibilityResult r : results) {
            String publicCode = "(null)";
            if (r.productCode() != null) {
                String[] parts = r.productCode().split("-");
                publicCode = parts.length >= 2 ? parts[0] + "-" + parts[1] : r.productCode();
            }
            sb.append(String.format("    %-16s | eligible=%-5b | roi=%-6s | reasons=%s\n",
                    publicCode, r.isEligible(),
                    r.roi() != null ? r.roi().setScale(2, RoundingMode.HALF_UP) : "N/A",
                    r.rejectionReasons() != null ? r.rejectionReasons().stream().limit(2).collect(Collectors.joining("; ")) : "—"));
        }
        return sb.toString();
    }
}
