package com.pryme.Backend.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_records",
        indexes = {
                // 🧠 THE DISPATCHER INDEX: Hyper-optimizes the SKIP LOCKED polling query
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),

                // 🧠 THE SWEEPER INDEX: Hyper-optimizes the Zombie Recovery query
                // Without this, the Sweeper Protocol requires a full table scan every 2 minutes!
                @Index(name = "idx_outbox_status_updated", columnList = "status, updated_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 🧠 ATOMIC CONCURRENCY SHIELD (Optimistic Locking)
    // Prevents lagging/zombie threads from overwriting a recovered event's status.
    @Version
    private Long version;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType; // e.g., "LOAN_APPLICATION"

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // e.g., "APPLICATION_APPROVED_EMAIL"

    // 🧠 JSONB PAYLOAD: Stores the email variables (Name, Amount, Bank) securely.
    // Extremely efficient in Postgres.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 🧠 REQUIRED FOR THE SWEEPER PROTOCOL
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = OutboxStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // 🧠 REQUIRED FIX: Added PROCESSING state to support the Claim-Check pattern
    public enum OutboxStatus {
        PENDING,
        PROCESSING, // Claimed by a Pod, currently executing network calls
        PROCESSED,
        FAILED
    }
}