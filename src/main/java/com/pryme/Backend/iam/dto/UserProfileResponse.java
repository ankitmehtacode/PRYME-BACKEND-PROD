package com.pryme.Backend.iam.dto;

import java.util.Map;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String city,
        String state,
        String profilePictureUrl,
        String role,
        Map<String, Object> metadata
) {
    public static UserProfileResponse from(com.pryme.Backend.iam.User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone() != null ? user.getPhone() : user.getPhoneNumber(),
                user.getCity(),
                user.getState(),
                user.getProfilePictureUrl(),
                user.getRole().name(),
                user.getMetadata()
        );
    }
}
