package com.pryme.Backend.calculators;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/calculators/prepayment")
@RequiredArgsConstructor
public class PrepaymentEngine {

    private final PrepaymentEngineService prepaymentEngineService;

    @PostMapping("/roi")
    public ResponseEntity<PrepaymentRoiResponse> roi(@Valid @RequestBody PrepaymentRoiRequest request) {
        return ResponseEntity.ok(prepaymentEngineService.calculateRoi(request));
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@Valid @RequestBody PrepaymentRoiRequest request) {
        PrepaymentRoiResponse response = prepaymentEngineService.calculateRoi(request);
        return ResponseEntity.ok(Map.of(
                "principalAmount", response.principalAmount(),
                "annualInterestRate", response.annualInterestRate(),
                "tenureMonths", response.tenureMonths(),
                "baselineInterest", response.baselineInterest(),
                "strategies", response.strategies()
        ));
    }
}

record PrepaymentRoiRequest(
        @NotNull @DecimalMin("10000.00") BigDecimal prepaymentAmount,
        @NotNull @DecimalMin("100000.00") BigDecimal principalAmount,
        @NotNull @DecimalMin("0.01") @DecimalMax("60.00") BigDecimal annualInterestRate,
        @NotNull @Min(12) @Max(480) Integer tenureMonths
) {
}

record PrepaymentRoiResponse(
        BigDecimal principalAmount,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal baselineInterest,
        List<StrategyImpact> strategies
) {
}

record StrategyImpact(
        String id,
        String name,
        String description,
        BigDecimal interestSaved,
        Integer timeTrimmedMonths,
        BigDecimal optimizedTotalInterest,
        boolean recommended
) {
}
