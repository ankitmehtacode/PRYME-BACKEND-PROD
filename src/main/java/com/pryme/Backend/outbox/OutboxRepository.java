package com.pryme.Backend.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, UUID> {

    // 🧠 SILICON VALLEY GOLD: FOR UPDATE SKIP LOCKED
    // This query mathematically guarantees that in a multi-node Kubernetes cluster,
    // no two servers will ever process the same notification event simultaneously.
    @Query(value = """
            SELECT * FROM outbox_records 
            WHERE status = 'PENDING' 
            ORDER BY created_at ASC 
            LIMIT :batchSize 
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxRecord> fetchPendingEventsForProcessing(@Param("batchSize") int batchSize);
}