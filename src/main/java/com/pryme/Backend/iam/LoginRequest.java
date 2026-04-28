package com.pryme.Backend.iam;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 🧠 LEAD HANDOFF: Optional leadId allows the frontend to pass the anonymous
 * lead UUID through the login flow. After successful authentication, the
 * backend stores this in the session so the frontend can auto-elevate it.
 */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        String deviceId,
        String leadId
) {}