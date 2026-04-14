package com.pryme.Backend.iam;

import java.util.List;
import java.util.UUID;

/**
 * 🧠 THE GOD OBJECT — Returned by GET /api/v1/auth/me
 * Hydrates the entire frontend identity layer in a single round-trip.
 *
 * Contract: Maps 1:1 to the frontend's MeResponse TypeScript interface.
 *           DO NOT rename fields without updating src/types/auth.types.ts
 */
public record MeResponse(
        UUID id,
        String email,
        String role,
        String name,             // 🧠 FIX: Was 'fullName' — frontend expects 'name'
        String phone,
        List<String> permissions  // 🧠 FIX: Added — frontend reads this for permission gating
) {}
