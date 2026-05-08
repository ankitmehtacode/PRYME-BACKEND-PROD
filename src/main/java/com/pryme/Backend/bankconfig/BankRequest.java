package com.pryme.Backend.bankconfig;

import jakarta.validation.constraints.NotBlank;

public record BankRequest(
        @NotBlank String bankName,
        String logoUrl,
        boolean isActive
) {
}
