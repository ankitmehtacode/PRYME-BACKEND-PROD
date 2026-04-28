package com.pryme.Backend.crm;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 🧠 SECURITY FIX: userId is NO LONGER accepted from the client.
 * The authenticated user's identity is extracted exclusively from
 * the server-side Authentication principal — preventing BOLA attacks
 * where User A could claim User B's anonymous lead.
 */
public record ElevateRequest(
        @NotNull(message = "Lead ID is strictly required for elevation")
        UUID leadId,

        String selectedBank
) {}