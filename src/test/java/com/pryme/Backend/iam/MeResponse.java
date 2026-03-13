package com.pryme.Backend.iam;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String email,
        String role,
        String fullName,
        String phone
) {}
