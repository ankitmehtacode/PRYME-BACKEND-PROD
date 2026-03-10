package com.pryme.Backend.calculators;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PrepaymentEngineServiceTest {

    private final PrepaymentEngineService service = new PrepaymentEngineService();

    @Test
    void calculateRoi_returnsThreeStrategiesAndRecommendedBest() {
        PrepaymentRoiRequest request = new PrepaymentRoiRequest(
                new BigDecimal("50000.00"),
                new BigDecimal("5000000.00"),
                new BigDecimal("8.50"),
                240
        );

        PrepaymentRoiResponse response = service.calculateRoi(request);

        assertThat(response.strategies()).hasSize(3);
        assertThat(response.strategies().stream().filter(StrategyImpact::recommended).count()).isEqualTo(1);
        assertThat(response.baselineInterest()).isPositive();
    }

    @Test
    void calculateRoi_producesPositiveSavingsForCombo() {
        PrepaymentRoiRequest request = new PrepaymentRoiRequest(
                new BigDecimal("100000.00"),
                new BigDecimal("3500000.00"),
                new BigDecimal("9.25"),
                180
        );

        PrepaymentRoiResponse response = service.calculateRoi(request);
        StrategyImpact combo = response.strategies().stream()
                .filter(s -> s.id().equals("combo"))
                .findFirst()
                .orElseThrow();

        assertThat(combo.interestSaved()).isPositive();
        assertThat(combo.timeTrimmedMonths()).isGreaterThanOrEqualTo(1);
    }
}
