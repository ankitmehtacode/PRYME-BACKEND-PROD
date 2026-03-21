package com.pryme.Backend.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<SessionRecord, UUID> {

    // ==========================================
    // 🧠 1. THE SNIPER QUERY (RAM OPTIMIZED)
    // 160 IQ FIX: Explicitly SELECT only the UUID.
    // Prevents Hibernate from mapping full entities into memory, reducing Heap allocation by ~90%.
    // Matches the updated SessionManager logic perfectly.
    // ==========================================
    @Query("SELECT s.id FROM SessionRecord s WHERE s.user.id = :userId AND s.isActive = true ORDER BY s.createdAt ASC")
    List<UUID> findActiveSessionIdsByUserId(@Param("userId") UUID userId);

    // ==========================================
    // 🧠 2. THE VACUUM PROTOCOL
    // 160 IQ FIX: flushAutomatically pushes pending inserts to DB first.
    // clearAutomatically purges the L1 Cache so the JVM doesn't hold references to deleted rows.
    // ==========================================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SessionRecord s WHERE s.expiresAt < :now OR s.isActive = false")
    int deleteByExpiresAtBeforeOrIsActiveFalse(@Param("now") Instant now);

    // ==========================================
    // 🧠 3. TOP 1% BULK UPDATE PROTOCOLS (MEMORY BYPASS)
    // By adding clearAutomatically = true, we guarantee that if another part of the code
    // queries these sessions within the same transaction, Hibernate won't return stale data.
    // ==========================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SessionRecord s SET s.isActive = false WHERE s.id IN :sessionIds")
    int deactivateSessionsBulk(@Param("sessionIds") List<UUID> sessionIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SessionRecord s SET s.isActive = false WHERE s.id = :sessionId")
    int deactivateSessionById(@Param("sessionId") UUID sessionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SessionRecord s SET s.isActive = false WHERE s.user.id = :userId")
    int deactivateAllByUserId(@Param("userId") UUID userId);
}