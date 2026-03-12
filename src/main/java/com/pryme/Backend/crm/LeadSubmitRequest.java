package com.pryme.Backend.crm;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.Map;

public record LeadSubmitRequest(
        @NotBlank(message = "userName is required")
        String userName,

        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "phone must be exactly 10 digits")
        String phone,

        @NotNull(message = "loanAmount is required")
        @DecimalMin(value = "10000.00", message = "Minimum loan amount is 10,000")
        BigDecimal loanAmount,

        @NotBlank(message = "loanType is required")
        String loanType,

        String offerId,

        // 🧠 THE DATA LOSS FIX: This captures the entire nested metadata object
        // sent by React (cibilScore, email, panCard, monthlyIncome) so it can
        // be safely serialized into the PostgreSQL/H2 database.
        Map<String, Object> metadata
) {
}