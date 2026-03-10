package com.pryme.Backend.document;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentMetadataResponse(
        UUID id,
        String applicationId,
        String docType,
        String originalFilename,
        String contentType,
        long fileSize,
        String storagePath,
        String checksum,
        LocalDateTime createdAt
) {
}
