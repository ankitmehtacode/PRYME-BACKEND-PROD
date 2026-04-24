package com.pryme.Backend.iam.dto;

import java.util.Map;

public record ProfileUpdateRequest(
        String fullName,
        String phone,
        String city,
        String state,
        String profilePictureUrl,
        Map<String, Object> metadata
) {}
