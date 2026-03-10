package com.pryme.Backend.crm;

import jakarta.validation.constraints.NotBlank;
public record UpdateStatusRequest(
        @NotBlank String status,
        Long version
) {
}
