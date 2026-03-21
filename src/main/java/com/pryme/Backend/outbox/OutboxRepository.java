package com.pryme.Backend.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, UUID> {

    // ==========================================
    // 🧠 1. THE ELASTIC DISPATCHER (Silicon Valley Gold)
    // Mathematically guarantees that in a multi-thread or multi-node environment,
    // no two workers will ever fetch the same pending notification.
    // ==========================================
    @Query(value = """
            SELECT * FROM outbox_records 
            WHERE status = 'PENDING' 
            ORDER BY created_at ASC 
            LIMIT :batchSize 
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxRecord> fetchPendingEventsForProcessing(@Param("batchSize") int batchSize);

    // ==========================================
    // 🧠 2. THE SWEEPER PROTOCOL (Zombie Recovery)
    // CRITICAL: If a network call to SendGrid/Twilio hangs and the Pod crashes,
    // this query identifies abandoned 'PROCESSING' events and safely reverts them
    // to 'PENDING' so the next healthy worker can retry them.
    // Uses strict Enum bindings to prevent Hibernate Startup Crashes.
    // ==========================================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OutboxRecord o SET o.status = :targetStatus WHERE o.status = :currentStatus AND o.updatedAt < :threshold")
    int resetStuckProcessingEvents(
            @Param("threshold") Instant threshold,
            @Param("targetStatus") OutboxRecord.OutboxStatus targetStatus,
            @Param("currentStatus") OutboxRecord.OutboxStatus currentStatus
    );
}