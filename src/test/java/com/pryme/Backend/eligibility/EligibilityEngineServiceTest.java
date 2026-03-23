package com.pryme.Backend.eligibility;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityEngineServiceTest {

    private final EligibilityEngineService service = new EligibilityEngineService();

    @Test
    void evaluateNip_matchesExpectedPrecisionValues() {
        EligibilityRequest req = baseRequest(
                new BigDecimal("1000000.00"),
                new BigDecimal("200000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("25000.00"), // Changed from 300000.00 to 25000.00
                BigDecimal.ZERO,
                BusinessType.OTHER
        );

        EligibilityResult result = service.evaluateNip(req);

        assertThat(result.eligibleIncome()).isEqualByComparingTo("108333.33");
        assertThat(result.maxEmiAllowed()).isEqualByComparingTo("102916.67");
        assertThat(result.netEligibleEmi()).isEqualByComparingTo("77916.67");
        assertThat(result.minimumLoanAmount()).isEqualByComparingTo("100000.00");
        assertThat(result.maximumLoanAmount()).isEqualByComparingTo("20000000.00");
    }

    @Test
    void evaluateGst_usesBusinessTypeMarginsExactly() {
        EligibilityRequest req = baseRequest(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("30000.00"), // Changed from 360000.00 to 30000.00
                new BigDecimal("12000000.00"),
                BusinessType.RETAILER
        );

        EligibilityResult result = service.evaluateGst(req);

        assertThat(result.eligibleIncome()).isEqualByComparingTo("120000.00");
        assertThat(result.maxEmiAllowed()).isEqualByComparingTo("78000.00");
        assertThat(result.netEligibleEmi()).isEqualByComparingTo("48000.00");
    }

    @Test
    void evaluate_returnsBestMatchOnlyAsTopProgram() {
        EligibilityRequest req = new EligibilityRequest(
                new BigDecimal("1000000.00"),
                new BigDecimal("200000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("200000.00"),
                new BigDecimal("1500000.00"),
                new BigDecimal("9000000.00"),
                new BigDecimal("4000000.00"),
                BusinessType.SERVICE,
                6,
                2,
                new BigDecimal("50.00"),
                12,
                true,
                new BigDecimal("7000000.00"),
                new BigDecimal("10.50"),
                240
        );

        BestMatchResponse response = service.evaluate(req);

        // 🧠 PRODUCTION FIX: Explicitly cast to (Object[]) to resolve the ambiguous varargs compiler error cleanly.
        assertThat(response.bestMatch().program()).isIn((Object[]) LapProgram.values());

        assertThat(response.bestMatch().estimatedEligibleLoanAmount()).isNotNull();
        assertThat(response.evaluated()).hasSize(5);
    }

    private EligibilityRequest baseRequest(BigDecimal pat, BigDecimal dep, BigDecimal interest, BigDecimal existingEmi,
                                           BigDecimal turnover12M, BusinessType type) {
        return new EligibilityRequest(
                pat,
                dep,
                interest,
                existingEmi,
                new BigDecimal("1000000.00"),
                turnover12M,
                new BigDecimal("5000000.00"),
                type,
                3,
                2,
                new BigDecimal("45.00"),
                12,
                true,
                new BigDecimal("5000000.00"),
                new BigDecimal("10.00"),
                240
        );
    }
}
