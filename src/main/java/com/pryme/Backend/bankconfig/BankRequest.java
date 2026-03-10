package com.pryme.Backend.bankconfig;

import jakarta.validation.constraints.NotBlank;

public record BankRequest(
        @NotBlank String bankName,
        @NotBlank String logoUrl,
        boolean isActive
) {
}
