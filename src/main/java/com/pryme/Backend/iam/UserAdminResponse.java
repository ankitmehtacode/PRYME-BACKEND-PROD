package com.pryme.Backend.iam;

import java.time.Instant;
import java.util.UUID;

/**
 * 🧠 ZERO-TRUST PROJECTION DTO
 * 
 * This record is the ONLY way user data leaves the IAM boundary for the Admin CRM.
 * It mathematically guarantees that passwordHash, version, and metadata
 * are NEVER serialized to the wire — even if Jackson is misconfigured.
 * 
 * Unlike a @JsonIgnore on the Entity (which is fragile and can be bypassed
 * by custom ObjectMappers), a projection record is structurally immune.
 */
public record UserAdminResponse(
    UUID id,
    String email,
    String fullName,
    String phoneNumber,
    String city,
    String state,
    Role role,
    Instant createdAt,
    Instant updatedAt
) {
    /**
     * Factory method: Entity → DTO boundary crossing.
     * This is the single choke-point for all IAM data egress.
     */
    public static UserAdminResponse from(User user) {
        return new UserAdminResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getCity(),
            user.getState(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
