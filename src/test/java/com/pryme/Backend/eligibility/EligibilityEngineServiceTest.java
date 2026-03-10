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
                new BigDecimal("300000.00"),
                BigDecimal.ZERO,
                BusinessType.OTHER
        );

        EligibilityResult result = service.evaluateNip(req);

        assertThat(result.eligibleIncome()).isEqualByComparingTo("1300000.00");
        assertThat(result.maxEmiAllowed()).isEqualByComparingTo("1235000.00");
        assertThat(result.netEligibleEmi()).isEqualByComparingTo("935000.00");
        assertThat(result.minimumLoanAmount()).isEqualByComparingTo("100000.00");
        assertThat(result.maximumLoanAmount()).isEqualByComparingTo("20000000.00");
    }

    @Test
    void evaluateGst_usesBusinessTypeMarginsExactly() {
        EligibilityRequest req = baseRequest(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("360000.00"),
                new BigDecimal("12000000.00"),
                BusinessType.RETAILER
        );

        EligibilityResult result = service.evaluateGst(req);

        assertThat(result.eligibleIncome()).isEqualByComparingTo("1440000.00");
        assertThat(result.maxEmiAllowed()).isEqualByComparingTo("936000.00");
        assertThat(result.netEligibleEmi()).isEqualByComparingTo("576000.00");
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

        assertThat(response.bestMatch().program()).isIn(LapProgram.values());
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
