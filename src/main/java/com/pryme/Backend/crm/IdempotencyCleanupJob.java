package com.pryme.Backend.crm;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class IdempotencyCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupJob.class);

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredIdempotencyKeys() {
        long deleted = idempotencyKeyRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(7));
        log.info("Idempotency cleanup removed {} keys older than 7 days", deleted);
    }
}
