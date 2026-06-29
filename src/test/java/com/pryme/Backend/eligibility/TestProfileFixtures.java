package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;

import java.math.BigDecimal;
import java.util.List;

/**
 * Type-safe factory for all 18 eligibility test profile payloads.
 *
 * <p>Each method constructs a fully-populated {@link EligibilityRequest} record
 * that maps 1:1 to the Test Profile Reference Document. No JSON parsing,
 * no markdown extraction, no reflection — just explicit Java construction.</p>
 *
 * <p>Naming convention: {@code profile{NN}()} where NN is the zero-padded profile ID.</p>
 */
public final class TestProfileFixtures {

    private TestProfileFixtures() {} // utility class

    // ─── DEFAULT IDEMPOTENCY KEY ─────────────────────────────────────────────
    private static final String IDEM = "test-profile-";

    // ─── HELPER: quick BigDecimal from int ───────────────────────────────────
    private static BigDecimal bd(long v) { return BigDecimal.valueOf(v); }
    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 01 — Salaried · Home Loan · NIP (Multi-Bank Aggregator)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile01() {
        return new EligibilityRequest(
                null,                        // lenderId (aggregator mode)
                "HOME_LOAN",                 // loanType
                750,                         // cibilScore
                32,                          // applicantAge
                "SALARIED",                  // employmentType
                "FLAT",                      // propertyType
                "TIER_1",                    // cityTier
                bd(3_500_000),               // loanAmount
                bd(5_000_000),               // propertyValue
                240,                         // requestedTenureMonths
                bd(85_000),                  // monthlyIncome
                bd(5_000),                   // existingEmiTotal
                0,                           // businessAgeYears
                8,                           // workExpYears (corrected from 0 to 8)
                new IncomeComputationInput(
                        "NIP", bd(1_020_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "01",                 // idempotencyKey
                null,                        // itrYearsAvailable
                bd(95_000),                  // grossMonthlyIncome (corrected from null)
                "452001",                    // pinCode
                null,                        // propertyCategory
                null                         // businessPropertyCategory
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 02 — Salaried · LAP · NIP (Multi-Bank)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile02() {
        return new EligibilityRequest(
                null, "LOAN_AGAINST_PROPERTY", 720, 45,
                "SALARIED", "HOME", null,
                bd(2_500_000), bd(6_000_000), 180,
                bd(75_000), bd(12_000),
                0, 20,                       // workExpYears (corrected from 0 to 20)
                new IncomeComputationInput(
                        "NIP", bd(900_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "02", null, bd(85_000), "452010", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 03 — Professional · Doctor · Home Loan · SENP
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile03() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 780, 38,
                "PROFESSIONAL", "VILLA", null,
                bd(5_000_000), bd(7_500_000), 240,
                bd(150_000), bd(8_000),
                10, 10,                      // workExpYears (corrected from 0 to 10)
                new IncomeComputationInput(
                        "SENP", null, null, null,
                        null, null, null, null,
                        bd(3_600_000),   // grossReceipts
                        "Doctor",        // profession
                        null, null
                ),
                IDEM + "03", 3, bd(200_000), "452003", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 04 — Professional · CA · LAP · SEP (Bank-Specific Multiplier)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile04() {
        return new EligibilityRequest(
                null, "LOAN_AGAINST_PROPERTY", 760, 42,
                "PROFESSIONAL", "APARTMENT", null,
                bd(4_000_000), bd(8_000_000), 180,
                bd(120_000), bd(15_000),
                15, 15,                      // workExpYears (corrected from 0 to 15)
                new IncomeComputationInput(
                        "SEP", null, null, null,
                        null, null, null, null,
                        bd(4_200_000),     // grossReceipts
                        "CA",              // profession
                        "L&T Finance",     // lenderName
                        "LAP"              // loanType
                ),
                IDEM + "04", 3, bd(180_000), "452009", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 05 — Self-Employed · ITR/NIP · Home Loan
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile05() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 700, 40,
                "SELF_EMPLOYED", "BUILDER_FLOOR", null,
                bd(4_500_000), bd(7_000_000), 240,
                bd(100_000), bd(10_000),     // monthlyIncome, existingEmiTotal (corrected from 0)
                12, 0,
                new IncomeComputationInput(
                        "NIP", bd(1_500_000), bd(200_000), bd(100_000),
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "05", 3, bd(130_000), "452012", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 06 — Self-Employed · GST Program · LAP (Commercial Property)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile06() {
        return new EligibilityRequest(
                null, "LOAN_AGAINST_PROPERTY", 680, 35,
                "SELF_EMPLOYED", "SHOP", null,
                bd(2_000_000), bd(5_000_000), 180,
                bd(60_000), bd(8_000),       // monthlyIncome, existingEmiTotal (corrected from 0)
                5, 0,
                new IncomeComputationInput(
                        "GST", null, null, null,
                        null, null,
                        bd(7_200_000),     // gstrTurnover12Months
                        "Retail",          // businessType
                        null, null, null, null
                ),
                IDEM + "06", 0, bd(80_000), "452018", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 07 — Self-Employed · Banking Program · Home Loan (ABB)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile07() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 730, 30,
                "SELF_EMPLOYED", "ROW_HOUSE", null,
                bd(2_000_000), bd(3_500_000), 240,
                bd(50_000), bd(0),           // monthlyIncome (corrected from 0)
                4, 0,
                new IncomeComputationInput(
                        "BANKING", null, null, null,
                        bd(1_000_000),     // averageBankBalance
                        List.of(bd(800_000), bd(1_200_000), bd(950_000), bd(1_100_000),
                                bd(900_000), bd(1_050_000), bd(1_000_000), bd(1_150_000)),
                        null, null, null, null, null, null
                ),
                IDEM + "07", null, bd(50_000), "453111", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 08 — Self-Employed · Cash Flow Program · LAP (Age Boundary)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile08() {
        return new EligibilityRequest(
                null, "LOAN_AGAINST_PROPERTY", 710, 50,
                "SELF_EMPLOYED", "WAREHOUSE", null,
                bd(3_000_000), bd(8_000_000), 120,
                bd(0), bd(20_000),
                20, 0,
                new IncomeComputationInput(
                        "CASHFLOW", null, null, null,
                        bd(500_000),       // averageBankBalance
                        null, null, null, null, null, null, null
                ),
                IDEM + "08", null, bd(0), "452020", null, null
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 09 — Professional · CS · Home Loan · CPM_SEP (Most Complex)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile09() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 770, 36,
                "PROFESSIONAL", "PENTHOUSE", null,
                bd(6_000_000), bd(10_000_000), 240,
                bd(0), bd(0),
                8, 0,
                new IncomeComputationInput(
                        "CPM_SEP",
                        bd(2_000_000),     // pat
                        bd(300_000),       // depreciation
                        null,
                        null, null, null, null,
                        bd(5_000_000),     // grossReceipts
                        "CS",              // profession
                        "HDFC Bank",       // lenderName
                        null
                ),
                IDEM + "09", 3, bd(0), "452005", null, null
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 10 — GEO-FENCE REJECTION — Non-Indore PIN (Mumbai)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile10() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 800, 30,
                "SALARIED", "FLAT", null,
                bd(2_500_000), bd(4_000_000), 240,
                bd(100_000), bd(0),          // monthlyIncome (corrected from 0)
                0, 6,                        // workExpYears (corrected from 0 to 6)
                new IncomeComputationInput(
                        "NIP", bd(1_200_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "10", null, bd(110_000), "400001", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 11 — Minimum Loan Amount Filter (₹15L)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile11() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 750, 28,
                "SALARIED", "FLAT", null,
                bd(1_500_000), bd(2_500_000), 240,
                bd(50_000), bd(0),
                0, 3,                        // workExpYears (corrected from 0 to 3, businessAgeYears to 0)
                new IncomeComputationInput(
                        "NIP", bd(600_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "11", null, bd(55_000), "452001", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 12 — Low CIBIL Score (660)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile12() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 660, 35,
                "SALARIED", "FLAT", null,
                bd(4_000_000), bd(6_000_000), 240,
                bd(90_000), bd(5_000),       // existingEmiTotal (corrected from 0)
                0, 6,                        // workExpYears (corrected from 0 to 6)
                new IncomeComputationInput(
                        "NIP", bd(1_080_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "12", null, bd(95_000), "452002", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 13 — Age-at-Maturity Restriction (48yr, 20yr tenure)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile13() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 770, 48,
                "SALARIED", "FLAT", null,
                bd(4_500_000), bd(7_000_000), 240,
                bd(100_000), bd(5_000),
                0, 15,                       // workExpYears (corrected from 0 to 15)
                new IncomeComputationInput(
                        "NIP", bd(1_200_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "13", null, bd(110_000), "452003", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 14 — Minimum Income Threshold (₹28K)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile14() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 750, 30,
                "SALARIED", "FLAT", null,
                bd(2_500_000), bd(4_500_000), 240,
                bd(28_000), bd(0),
                0, 4,                        // workExpYears (corrected from 0 to 4)
                new IncomeComputationInput(
                        "NIP", bd(336_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "14", null, bd(32_000), "452001", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 15 — Property Type Restriction (PLOT)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile15() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 760, 32,
                "SALARIED", "PLOT", null,
                bd(4_000_000), bd(7_000_000), 240,
                bd(85_000), bd(5_000),
                0, 6,                        // workExpYears (corrected from 0 to 6)
                new IncomeComputationInput(
                        "NIP", bd(1_020_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "15", null, bd(95_000), "452001", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 16 — Combined: Low CIBIL (660) + Low Income (₹32K)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile16() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 660, 35,
                "SALARIED", "FLAT", null,
                bd(3_000_000), bd(5_000_000), 240,
                bd(32_000), bd(0),
                0, 6,                        // workExpYears (corrected from 0 to 6)
                new IncomeComputationInput(
                        "NIP", bd(384_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "16", null, bd(38_000), "452002", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 17 — SE GST — Low Turnover + Short Vintage (2yr)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile17() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 730, 34,
                "SELF_EMPLOYED", "FLAT", null,
                bd(3_000_000), bd(6_000_000), 240,
                bd(30_000), bd(0),
                2, 0,
                new IncomeComputationInput(
                        "GST", null, null, null,
                        null, null,
                        bd(4_800_000),     // gstrTurnover12Months
                        "Wholesale",       // businessType
                        null, null, null, null
                ),
                IDEM + "17", null, bd(45_000), "452005", null, null // grossMonthlyIncome (corrected from null)
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PROFILE 18 — LOW_LTV Surrogate Fallback (CIBIL 660, LTV 37.5%)
    // ═════════════════════════════════════════════════════════════════════════
    public static EligibilityRequest profile18() {
        return new EligibilityRequest(
                null, "HOME_LOAN", 660, 30,
                "SALARIED", "FLAT", null,
                bd(3_000_000), bd(8_000_000), 240,
                bd(60_000), bd(5_000),
                0, 5,                        // workExpYears (corrected from 0 to 5)
                new IncomeComputationInput(
                        "NIP", bd(720_000), null, null,
                        null, null, null, null, null, null, null, null
                ),
                IDEM + "18", null, bd(65_000), "452003", null, null // grossMonthlyIncome (corrected from null)
        );
    }
}
