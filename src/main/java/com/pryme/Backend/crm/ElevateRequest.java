package com.pryme.Backend.crm;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ElevateRequest(
        @NotNull(message = "Lead ID is strictly required for elevation")
        UUID leadId,

        @NotNull(message = "Authenticated User ID is strictly required to bind the application")
        UUID userId
) {}