package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.service.SurrogateIncomeResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SurrogateIncomeResolver — SEP & CPM_SEP Policies")
class SurrogateIncomeResolverTest {

    private final SurrogateIncomeResolver resolver = new SurrogateIncomeResolver();

    @Test
    @DisplayName("SEP Surrogate — L&T Finance doctor / ca / cs multiplier computation")
    void testSepLtFinance() {
        // L&T Doctor / CA multiplier is 2.5. Gross Receipts: 120,000. Expected monthly: 120,000 * 2.5 / 12 = 25,000
        IncomeComputationInput doctorInput = new IncomeComputationInput(
                "SEP",
                BigDecimal.ZERO, // pat
                BigDecimal.ZERO, // depreciation
                BigDecimal.ZERO, // interestExpense
                BigDecimal.ZERO, // averageBankBalance
                new ArrayList<>(), // bankBalanceSamples
                BigDecimal.ZERO, // gstrTurnover12Months
                "", // businessType
                new BigDecimal("120000"), // grossReceipts
                "Doctor", // profession
                "L&T Finance", // lenderName
                "HL" // loanType
        );

        BigDecimal result = resolver.resolve(doctorInput);
        assertEquals(0, new BigDecimal("25000").compareTo(result), "Expected 2.5x multiplier for L&T Doctor");

        // L&T CS multiplier is 1.5. Gross Receipts: 120,000. Expected monthly: 120,000 * 1.5 / 12 = 15,000
        IncomeComputationInput csInput = new IncomeComputationInput(
                "SEP",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                BigDecimal.ZERO,
                "",
                new BigDecimal("120000"), // grossReceipts
                "Company Secretary", // profession (normalized to cs)
                "L&T Finance", // lenderName
                "HL" // loanType
        );

        BigDecimal csResult = resolver.resolve(csInput);
        assertEquals(0, new BigDecimal("15000").compareTo(csResult), "Expected 1.5x multiplier for L&T CS");
    }

    @Test
    @DisplayName("SEP Surrogate — JIO Finance CA loanType override (HL=3.0 vs LAP=2.0)")
    void testSepJioFinanceLoanTypeOverride() {
        // JIO HL CA -> 3.0x multiplier. Expected monthly: 120,000 * 3 / 12 = 30,000
        IncomeComputationInput hlInput = new IncomeComputationInput(
                "SEP",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                BigDecimal.ZERO,
                "",
                new BigDecimal("120000"),
                "CA",
                "JIO Finance",
                "HL"
        );

        BigDecimal hlResult = resolver.resolve(hlInput);
        assertEquals(0, new BigDecimal("30000").compareTo(hlResult), "Expected 3x multiplier for JIO HL CA");

        // JIO LAP CA -> 2.0x multiplier. Expected monthly: 120,000 * 2 / 12 = 20,000
        IncomeComputationInput lapInput = new IncomeComputationInput(
                "SEP",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                BigDecimal.ZERO,
                "",
                new BigDecimal("120000"),
                "CA",
                "JIO Finance",
                "LAP"
        );

        BigDecimal lapResult = resolver.resolve(lapInput);
        assertEquals(0, new BigDecimal("20000").compareTo(lapResult), "Expected 2x multiplier for JIO LAP CA");
    }

    @Test
    @DisplayName("CPM_SEP Surrogate — Yes Bank doctor / ca multiplier and LAP cap rule")
    void testCpmYesBank() {
        // Yes Bank Doctor CPM multiplier is 4. (PAT 50,000 + Depr 10,000) * 4 / 12 = 20,000
        IncomeComputationInput docInput = new IncomeComputationInput(
                "CPM_SEP",
                new BigDecimal("50000"), // pat
                new BigDecimal("10000"), // depreciation
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                BigDecimal.ZERO,
                "",
                new BigDecimal("1000000"), // grossReceipts (large so no cap)
                "Doctor",
                "Yes Bank",
                "LAP"
        );

        BigDecimal docResult = resolver.resolve(docInput);
        assertEquals(0, new BigDecimal("20000").compareTo(docResult), "Expected (50k+10k)*4/12 = 20k");

        // Yes Bank CA CPM multiplier is 3. (PAT 100,000 + Depr 20,000) * 3 = 360,000 annual.
        // If Gross Receipts = 300,000, LAP cap rule applies: capped annual income = Gross Receipts (300,000) -> 300,000 / 12 = 25,000
        IncomeComputationInput caInputCapped = new IncomeComputationInput(
                "CPM_SEP",
                new BigDecimal("100000"), // pat
                new BigDecimal("20000"), // depreciation
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                BigDecimal.ZERO,
                "",
                new BigDecimal("300000"), // grossReceipts
                "CA",
                "Yes Bank",
                "LAP"
        );

        BigDecimal cappedResult = resolver.resolve(caInputCapped);
        assertEquals(0, new BigDecimal("25000").compareTo(cappedResult), "Expected capped monthly income of 25k");

        // For Yes Bank HL CA, the LAP cap rule does NOT apply. (PAT 100,000 + Depr 20,000) * 3 / 12 = 30,000.
        // Gross Receipts = 300,000. Capped annual would have been 300k, but since HL, it should be 30,000 (360k / 12).
        IncomeComputationInput caInputUncappedHl = new IncomeComputationInput(
                "CPM_SEP",
                new BigDecimal("100000"), // pat
                new BigDecimal("20000"), // depreciation
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                BigDecimal.ZERO,
                "",
                new BigDecimal("300000"), // grossReceipts
                "CA",
                "Yes Bank",
                "HL"
        );

        BigDecimal uncappedResult = resolver.resolve(caInputUncappedHl);
        assertEquals(0, new BigDecimal("30000").compareTo(uncappedResult), "Expected uncapped monthly income of 30k for HL");
    }

    @Test
    @DisplayName("GST Surrogate — dynamic lender and loanType profit margin lookup validation")
    void testGstLenderSpecificMargins() {
        // L&T: Service=10%, Retailer=12%, Wholesale=8%, Manufacturer=4%
        // Turnover = 1,200,000. Service monthly = 1.2M * 0.10 / 12 = 10,000.
        IncomeComputationInput ltService = createGstInput("L&T Finance", "HL", "Service", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("10000").compareTo(resolver.resolve(ltService)));

        // ICICI: Manufacturer = 6% -> 1.2M * 0.06 / 12 = 6,000.
        IncomeComputationInput iciciMfg = createGstInput("ICICI Bank", "HL", "Manufacturer", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("6000").compareTo(resolver.resolve(iciciMfg)));

        // Bandhan: GST = 10% -> 1.2M * 0.10 / 12 = 10,000.
        IncomeComputationInput bandhanDefault = createGstInput("Bandhan Bank", "HL", "Retailer", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("10000").compareTo(resolver.resolve(bandhanDefault)));

        // Yes Bank: GST = 20% -> 1.2M * 0.20 / 12 = 20,000.
        IncomeComputationInput yesDefault = createGstInput("Yes Bank", "HL", "Service", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("20000").compareTo(resolver.resolve(yesDefault)));

        // HDFC HL: Trader = 8% -> 1.2M * 0.08 / 12 = 8,000.
        IncomeComputationInput hdfcHl = createGstInput("HDFC Bank", "HL", "Trader", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("8000").compareTo(resolver.resolve(hdfcHl)));

        // HDFC LAP: Trader = 9% -> 1.2M * 0.09 / 12 = 9,000.
        IncomeComputationInput hdfcLapTrader = createGstInput("HDFC Bank", "LAP", "Trader", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("9000").compareTo(resolver.resolve(hdfcLapTrader)));

        // HDFC LAP: Manufacturer = 10% -> 1.2M * 0.10 / 12 = 10,000.
        IncomeComputationInput hdfcLapMfg = createGstInput("HDFC Bank", "LAP", "Manufacturer", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("10000").compareTo(resolver.resolve(hdfcLapMfg)));

        // HDFC LAP: Service = 8% -> 1.2M * 0.08 / 12 = 8,000.
        IncomeComputationInput hdfcLapService = createGstInput("HDFC Bank", "LAP", "Service", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("8000").compareTo(resolver.resolve(hdfcLapService)));

        // Bajaj: Trader/Service = 10% -> 1.2M * 0.10 / 12 = 10,000.
        IncomeComputationInput bajajTrader = createGstInput("Bajaj Finance", "LAP", "Trader", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("10000").compareTo(resolver.resolve(bajajTrader)));

        // Bajaj: Wholesale/Manufacturer = 8% -> 1.2M * 0.08 / 12 = 8,000.
        IncomeComputationInput bajajWholesale = createGstInput("Bajaj Finance", "LAP", "Wholesale", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("8000").compareTo(resolver.resolve(bajajWholesale)));

        // IDFC: Manufacturer = 10% -> 1.2M * 0.10 / 12 = 10,000.
        IncomeComputationInput idfcMfg = createGstInput("IDFC Bank", "LAP", "Manufacturer", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("10000").compareTo(resolver.resolve(idfcMfg)));

        // IDFC: Trader/Service = 7% -> 1.2M * 0.07 / 12 = 7,000.
        IncomeComputationInput idfcService = createGstInput("IDFC Bank", "LAP", "Service", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("7000").compareTo(resolver.resolve(idfcService)));

        // JIO: Trader = 6% -> 1.2M * 0.06 / 12 = 6,000.
        IncomeComputationInput jioTrader = createGstInput("JIO Finance", "HL", "Trader", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("6000").compareTo(resolver.resolve(jioTrader)));

        // JIO: Manufacturer = 8% -> 1.2M * 0.08 / 12 = 8,000.
        IncomeComputationInput jioMfg = createGstInput("JIO Finance", "HL", "Manufacturer", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("8000").compareTo(resolver.resolve(jioMfg)));

        // JIO: Service = 12% -> 1.2M * 0.12 / 12 = 12,000.
        IncomeComputationInput jioService = createGstInput("JIO Finance", "HL", "Service", new BigDecimal("1200000"));
        assertEquals(0, new BigDecimal("12000").compareTo(resolver.resolve(jioService)));
    }

    private IncomeComputationInput createGstInput(String lenderName, String loanType, String businessType, BigDecimal turnover) {
        return new IncomeComputationInput(
                "GST",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new ArrayList<>(),
                turnover,
                businessType,
                BigDecimal.ZERO,
                "",
                lenderName,
                loanType
        );
    }
}
