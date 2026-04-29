package com.pryme.Backend.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DocumentUploadRequest(
        @NotBlank(message = "applicationId is required") String applicationId,
        @NotBlank(message = "docType is required") String docType,
        @NotBlank(message = "contentType is required") String contentType,
        @NotBlank(message = "filename is required") String filename,
        @NotNull(message = "fileSize is required") @PositiveOrZero long fileSize
) {
}
