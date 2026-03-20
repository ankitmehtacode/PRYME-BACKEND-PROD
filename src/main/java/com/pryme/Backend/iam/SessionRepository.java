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
    // 🧠 1. THE SNIPER QUERY
    // Fetches active sessions for a user, oldest first.
    // Used by SessionManager to identify which sessions to kill when the Max Devices limit is hit.
    // ==========================================
    List<SessionRecord> findAllByUser_IdAndIsActiveTrueOrderByCreatedAtAsc(UUID userId);

    // ==========================================
    // 🧠 2. THE VACUUM PROTOCOL
    // Instead of fetching 10,000 expired rows into Java RAM and calling repository.deleteAll(),
    // this executes a single, hyper-fast native SQL DELETE statement directly on the DB engine.
    // ==========================================
    @Modifying
    @Query("DELETE FROM SessionRecord s WHERE s.expiresAt < :now OR s.isActive = false")
    int deleteByExpiresAtBeforeOrIsActiveFalse(@Param("now") Instant now);

    // ==========================================
    // 🧠 3. TOP 1% BULK UPDATE PROTOCOLS (MEMORY BYPASS)
    // 99% of developers fetch rows into RAM, call setStatus(false), and save them back.
    // These methods bypass the JVM L1 Cache completely, executing atomic SQL updates
    // directly on the PostgreSQL engine to prevent OutOfMemoryErrors and Deadlocks.
    // ==========================================

    @Modifying
    @Query("UPDATE SessionRecord s SET s.isActive = false WHERE s.id IN :sessionIds")
    int deactivateSessionsBulk(@Param("sessionIds") List<UUID> sessionIds);

    @Modifying
    @Query("UPDATE SessionRecord s SET s.isActive = false WHERE s.id = :sessionId")
    int deactivateSessionById(@Param("sessionId") UUID sessionId);

    @Modifying
    @Query("UPDATE SessionRecord s SET s.isActive = false WHERE s.user.id = :userId")
    int deactivateAllByUserId(@Param("userId") UUID userId);
}