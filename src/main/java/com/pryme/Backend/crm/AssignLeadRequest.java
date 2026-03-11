package com.pryme.Backend.crm;

import jakarta.validation.constraints.NotBlank;
public record AssignLeadRequest(
        @NotBlank String assigneeId,
        Long version
) {
}
