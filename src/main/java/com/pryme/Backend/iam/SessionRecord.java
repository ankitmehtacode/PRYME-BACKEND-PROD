package com.pryme.Backend.iam;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "session_records",
        indexes = {
                // 🧠 THE SNIPER INDEX:
                // Makes the "Max Devices" query lightning fast (< 2ms).
                // PostgreSQL can instantly find a user's oldest active session without a table scan.
                @Index(name = "idx_session_user_active", columnList = "user_id, is_active, created_at"),

                // 🧠 THE VACUUM INDEX:
                // Prevents DB CPU spikes when the @Scheduled Cron Job runs every 5 minutes.
                // Instantly locates expired rows to wipe them off the disk.
                @Index(name = "idx_session_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRecord {

    // 🧠 The JTI (JWT ID) acts as the primary key. This physically links the stateless JWT to the DB state.
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", length = 45) // 45 chars safely accommodates lengthy IPv6 addresses
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ==========================================
    // JPA LIFECYCLE HOOKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    // ==========================================
    // 🧠 DOMAIN LOGIC
    // ==========================================
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    // ==========================================
    // 🧠 FAILPROOF JPA EQUALITY
    // NEVER use Lombok's @Data or @EqualsAndHashCode on JPA entities.
    // We bind equality strictly to the JWT ID to prevent memory leaks in Hibernate's L1 Cache.
    // ==========================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionRecord that = (SessionRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}