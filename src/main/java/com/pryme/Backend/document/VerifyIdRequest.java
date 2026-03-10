package com.pryme.Backend.document;

import jakarta.validation.constraints.NotBlank;

public record VerifyIdRequest(
        @NotBlank String applicationId,
        @NotBlank String idType,
        @NotBlank String idNumber
) {
}
