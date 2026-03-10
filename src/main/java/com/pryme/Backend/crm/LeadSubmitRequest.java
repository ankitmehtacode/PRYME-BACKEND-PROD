package com.pryme.Backend.crm;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record LeadSubmitRequest(
        @NotBlank String userName,
        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "phone must be 10 digits") String phone,
        @NotNull @DecimalMin(value = "10000.00") BigDecimal loanAmount,
        @NotBlank String loanType,
        String offerId
) {
}
