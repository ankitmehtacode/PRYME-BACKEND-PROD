package com.pryme.Backend.eligibility.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record EligibilityRequest(
        Long lenderId,
        @NotNull String loanType,
        @NotNull @Min(300) @Max(900) int cibilScore,
        @NotNull @Min(18) int applicantAge,
        @NotNull String employmentType,
        @NotNull String propertyType,
        @NotNull String cityTier,
        @NotNull @DecimalMin("10000.00") BigDecimal loanAmount,
        @NotNull @DecimalMin("0.00") BigDecimal propertyValue,
        @NotNull @Min(12) int requestedTenureMonths,
        @NotNull @DecimalMin("0.00") BigDecimal monthlyIncome,
        @NotNull @DecimalMin("0.00") BigDecimal existingEmiTotal,
        @NotNull @Min(0) int businessAgeYears,
        @NotNull @Min(0) int workExpYears,
        @NotNull IncomeComputationInput incomeComputationInput,
        @NotNull String idempotencyKey
) {
}
