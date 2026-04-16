package com.pryme.Backend.crm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKeyHash(String keyHash);
    long deleteByCreatedAtBefore(LocalDateTime threshold);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "INSERT INTO idempotency_keys (key_hash, status, created_at) VALUES (:keyHash, 'IN_PROGRESS', NOW())", nativeQuery = true)
    void insertInProgressKey(@org.springframework.data.repository.query.Param("keyHash") String keyHash);
}
