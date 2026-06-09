package com.pryme.Backend.bankconfig;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record BankRequest(
        @NotBlank String bankName,
        String logoUrl,
        @JsonAlias({"active", "isActive"}) boolean active
) {
}
