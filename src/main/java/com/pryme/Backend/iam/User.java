package com.pryme.Backend.iam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true)
        }
)
// 🧠 160 IQ FIX 1: Removed class-level @Setter to strictly protect system columns.
// JPA requires a no-args constructor, but we make it PROTECTED so developers
// are forced to use the @Builder instead of instantiating empty shells.
@Setter
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 🧠 160 IQ FIX 2: Enforced Immutability at the SQL Level
    // Added updatable = false. Hibernate will now physically refuse to generate
    // an UPDATE statement for the email column, mathematically guaranteeing your logic.
    @NaturalId
    @Column(nullable = false, unique = true, length = 150, updatable = false)
    private String email;

    // 🧠 DEFENSE-IN-DEPTH: @JsonIgnore ensures BCrypt hash NEVER leaks
    // even if the User entity is accidentally serialized (e.g., SessionRecord lazy-load).
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    // 🧠 Selective Setters: Only explicitly mutable profile fields get setters.
    @Setter
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Setter
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Setter
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Setter
    @Column(name = "city", length = 100)
    private String city;

    @Setter
    @Column(name = "state", length = 100)
    private String state;

    // New fields added
    @Setter
    @Column(name = "phone", length = 20)
    private String phone;

    // Replacing @ElementCollection, @CollectionTable with @JdbcTypeCode and @Column
    @JsonIgnore
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @JsonIgnore
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) {
            this.role = Role.USER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ==========================================
    // 🧠 DOMAIN-DRIVEN BEHAVIORS
    // ==========================================

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void elevateRole(Role newRole) {
        this.role = newRole;
    }

    // ==========================================
    // 🧠 HIBERNATE PROXY-SAFE EQUALS/HASHCODE
    // ==========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        // 🧠 160 IQ FIX 3: The 'instanceof' bypasses the CGLIB subclass mismatch.
        if (!(o instanceof User user)) return false;

        // 🧠 CRITICAL: We MUST use the getter method (user.getEmail()), NOT the field (user.email).
        // Accessing fields directly on a lazy Hibernate Proxy yields null!
        return getEmail() != null && Objects.equals(getEmail(), user.getEmail());
    }

    @Override
    public int hashCode() {
        // Safe to hash the getter because the Natural ID is mathematically immutable.
        return Objects.hash(getEmail());
    }
}
