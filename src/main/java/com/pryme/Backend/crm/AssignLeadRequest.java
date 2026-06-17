package com.pryme.Backend.crm;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

/**
 * 🧠 Assignment request DTO.
 * version is optional for assignment operations (last-write-wins semantics).
 * It remains in the contract for backward compatibility with older frontend builds.
 */
public record AssignLeadRequest(
        @Nullable String assigneeId,
        @Nullable Long version
) {
}
