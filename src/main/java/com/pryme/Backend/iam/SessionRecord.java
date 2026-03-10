package com.pryme.Backend.iam;

import java.time.Instant;
import java.util.UUID;

public record SessionRecord(
        String token,
        UUID userId,
        String deviceId,
        Instant createdAt,
        Instant expiresAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
