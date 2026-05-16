package com.pryme.Backend.iam.dto;

import com.pryme.Backend.iam.User;

import java.util.UUID;

/**
 * 🧠 ZERO-ALLOCATION DROPDOWN DTO:
 * Minimal projection for the lead-assignment dropdown selector.
 * Only exposes id, fullName, and role — no sensitive data crosses the wire.
 * This is the ONLY shape the frontend sees for team member selection.
 */
public record TeamMemberOption(
        UUID id,
        String fullName,
        String role
) {
    public static TeamMemberOption from(User user) {
        return new TeamMemberOption(
                user.getId(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
