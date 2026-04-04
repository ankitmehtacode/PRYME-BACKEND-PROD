package com.pryme.Backend.crm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKeyHash(String keyHash);
    long deleteByCreatedAtBefore(LocalDateTime threshold);
}
