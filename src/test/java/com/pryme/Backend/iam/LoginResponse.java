package com.pryme.Backend.iam;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        UUID id, // 🧠 CRITICAL: The Elevation Engine needs this ID
        String token,
        String role,
        String name,
        Instant expiresAt,
        String message
) {}