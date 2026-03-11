package com.pryme.Backend.eligibility;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EligibilityRequest(
        @NotNull @DecimalMin("0.00") BigDecimal pat,
        @NotNull @DecimalMin("0.00") BigDecimal depreciation,
        @NotNull @DecimalMin("0.00") BigDecimal interest,
        @NotNull @DecimalMin("0.00") BigDecimal existingEmis,

        @NotNull @DecimalMin("0.00") BigDecimal avgBankCredits,
        @NotNull @DecimalMin("0.00") BigDecimal turnover12M,
        @NotNull @DecimalMin("0.00") BigDecimal grossReceipts,

        @NotNull BusinessType businessType,
        @NotNull @Min(0) Integer businessVintageYears,
        @NotNull @Min(0) Integer itrYears,

        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal requestedLtvPercent,
        @NotNull @Min(0) @Max(120) Integer emiObligationAgeMonths,
        @NotNull Boolean residentialProperty,

        @NotNull @DecimalMin("0.00") BigDecimal requestedLoanAmount,
        @NotNull @DecimalMin("0.01") @DecimalMax("60.00") BigDecimal annualInterestRate,
        @NotNull @Min(1) @Max(480) Integer tenureMonths
) {
}
