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
                // 🧠 THE DISPATCHER INDEX: Hyper-optimizes the polling query
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
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

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType; // e.g., "LOAN_APPLICATION"

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // e.g., "APPLICATION_APPROVED_EMAIL"

    // 🧠 JSONB PAYLOAD: Stores the email variables (Name, Amount, Bank) securely
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) this.status = OutboxStatus.PENDING;
    }

    public enum OutboxStatus {
        PENDING, PROCESSED, FAILED
    }
}