package com.pryme.Backend.document;

import jakarta.validation.constraints.NotBlank;

public record DocumentUploadRequest(
        @NotBlank(message = "documentId is required")
        String documentId,
        @NotBlank(message = "contentType is required")
        String contentType
) {
}
