package com.pryme.Backend.iam;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                // 🧠 Indexing the Natural ID ensures hyper-fast login queries
                @Index(name = "idx_user_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 🧠 NATURAL ID: The top 1% practice for JPA equals/hashCode.
    // Emails never change in our system logic without a strict migration, making them a perfect natural identifier.
    @NaturalId
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Secured password payload
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    // ==========================================
    // 🧠 EXTENDED PROFILE MATRIX
    // ==========================================

    // Upgraded from 'phone' to 'phone_number' to match our strict DTO standards
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    // ==========================================
    // 🧠 CONCURRENCY & TELEMETRY ENGINE
    // ==========================================

    // OPTIMISTIC LOCKING: Prevents "Lost Updates".
    // If an Admin edits the profile at the exact same millisecond the User edits it,
    // Hibernate will safely reject one instead of corrupting the database.
    @Version
    private Long version;

    // Using Instant instead of LocalDateTime guarantees global UTC standardization
    // regardless of what timezone the AWS/Linux server is running in.
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
    // 🧠 FAILPROOF JPA EQUALS/HASHCODE
    // ==========================================
    // We explicitly avoid Lombok's @EqualsAndHashCode.
    // By using the @NaturalId (email) for equality, we guarantee that Hibernate sets
    // and caching mechanisms never lose track of the entity across persistent states.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}