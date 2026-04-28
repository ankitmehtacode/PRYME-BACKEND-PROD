package com.pryme.Backend.iam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 🧠 160 IQ LOGIN RESPONSE — Carries the FULL user profile on login.
 *
 * WHY: The Vite dev-server proxy does NOT reliably forward Set-Cookie headers,
 * so the subsequent /auth/me call gets a 401 (cookie missing). By embedding
 * the full MeResponse payload directly in the login response, the frontend
 * can hydrate its React Query cache in a SINGLE round-trip — zero cookie
 * dependency for the initial redirect.
 *
 * Contract: The `user` field maps 1:1 to the frontend's MeResponse interface.
 *
 * 🧠 LEAD HANDOFF: `pendingLeadId` is set when the user logged in with an
 * anonymous lead UUID. The frontend reads this and auto-calls /elevate.
 * Will be null if no lead was attached to the login request.
 */
public record LoginResponse(
        UUID id,
        String role,
        String name,
        Instant expiresAt,
        String message,
        MeResponse user,
        String pendingLeadId
) {}