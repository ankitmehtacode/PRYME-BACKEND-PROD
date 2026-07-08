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
        @NotNull @com.pryme.Backend.common.validation.ValidCibilScore int cibilScore,
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
        @NotNull String idempotencyKey,
        // ── Optional fields — engine enforces only when non-null ──
        Integer itrYearsAvailable,           // Applicant's ITR filing years
        BigDecimal grossMonthlyIncome,       // Declared gross monthly income (for minIncome check fallback)
        String pinCode,                      // 6-digit PIN code — used for geo-fencing (Indore-only)
        String propertyCategory,
        String businessPropertyCategory
) {
    public Integer itrYearsAvailable() {
        return itrYearsAvailable == null ? 0 : itrYearsAvailable;
    }

    public BigDecimal grossMonthlyIncome() {
        return grossMonthlyIncome == null ? BigDecimal.ZERO : grossMonthlyIncome;
    }
}
