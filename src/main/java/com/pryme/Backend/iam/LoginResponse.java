package com.pryme.Backend.iam;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        UUID id, // 🧠 CRITICAL: The Elevation Engine needs this ID
        // 🧠 SECURITY FIX: `token` field REMOVED.
        // Session ID is now transported exclusively via HttpOnly cookie (Set-Cookie header).
        // This physically prevents XSS payloads from reading the session token via localStorage.
        String role,
        String name,
        Instant expiresAt,
        String message
) {}