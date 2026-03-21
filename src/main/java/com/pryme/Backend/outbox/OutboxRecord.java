package com.pryme.Backend.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_records",
        indexes = {
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
                @Index(name = "idx_outbox_status_updated", columnList = "status, updated_at")
        }
)
// 🧠 160 IQ FIX 1: Removed class-level @Setter.
// Core event data (Payload, AggregateID) is now mathematically IMMUTABLE after creation.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "aggregate_type", nullable = false, length = 50, updatable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100, updatable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100, updatable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false, updatable = false)
    private String payload;

    // 🧠 Selective Setters: Only lifecycle fields are allowed to mutate
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Setter
    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // 🧠 160 IQ FIX 2: Poison Pill Defense (Tracks execution attempts)
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = OutboxStatus.PENDING;
        if (this.retryCount == null) this.retryCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ==========================================
    // 🧠 DOMAIN-DRIVEN DESIGN ENFORCEMENTS
    // ==========================================

    /**
     * 160 IQ FIX 3: Self-Healing Setter
     * Enforces the 1000-char DB limit INSIDE the entity.
     * No developer can ever crash the SQL driver by passing a massive stack trace.
     */
    public void setErrorMessage(String errorMessage) {
        if (errorMessage != null && errorMessage.length() > 950) {
            this.errorMessage = errorMessage.substring(0, 950) + "...[TRUNCATED]";
        } else {
            this.errorMessage = errorMessage;
        }
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    // ==========================================
    // 🧠 HIBERNATE PROXY SAFETY
    // Prevents Set/List corruption when managing Entities in RAM
    // ==========================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxRecord that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum OutboxStatus {
        PENDING,
        PROCESSING,
        PROCESSED,
        FAILED
    }
}