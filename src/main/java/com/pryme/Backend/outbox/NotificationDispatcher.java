package com.pryme.Backend.outbox;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final OutboxRepository outboxRepository;
    private final TransactionTemplate transactionTemplate;

    // Inject your EmailSenderService / SMSService here
    // private final EmailService emailService;

    // 🧠 160 IQ FIX 1: The Resiliency Protocol (Max Retries)
    // Determines how many times we survive a SendGrid/Twilio network failure before giving up.
    private static final int MAX_RETRIES = 3;

    @Scheduled(fixedDelayString = "${app.outbox.poll-rate:5000}", initialDelayString = "#{new java.util.Random().nextInt(5000)}")
    public void processOutboxEvents() {

        transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        List<OutboxRecord> claimedEvents = transactionTemplate.execute(status -> {
            List<OutboxRecord> pending = outboxRepository.fetchPendingEventsForProcessing(50);
            for (OutboxRecord event : pending) {
                event.setStatus(OutboxRecord.OutboxStatus.PROCESSING);
                event.incrementRetryCount(); // 🧠 TRACK ATTEMPTS: Binds to the new Entity method
            }
            return outboxRepository.saveAll(pending);
        });

        if (claimedEvents == null || claimedEvents.isEmpty()) return;

        log.debug("Outbox Engine: Claimed {} events. Executing network dispatch...", claimedEvents.size());

        for (OutboxRecord event : claimedEvents) {
            try {
                dispatchNetworkCall(event);

                transactionTemplate.executeWithoutResult(status ->
                        updateEventStatus(event, OutboxRecord.OutboxStatus.PROCESSED, null)
                );

            } catch (Exception ex) {
                log.error("Outbox Engine: Failed to dispatch event ID {} (Attempt {}/{})",
                        event.getId(), event.getRetryCount(), MAX_RETRIES, ex);

                // 🧠 160 IQ FIX 2: Dynamic Fallback Routing
                // If we haven't hit the limit, route it BACK to 'PENDING' so the next cycle retries it.
                // If we have hit the limit, route it to 'FAILED' to stop infinite loops.
                OutboxRecord.OutboxStatus nextStatus = (event.getRetryCount() >= MAX_RETRIES)
                        ? OutboxRecord.OutboxStatus.FAILED
                        : OutboxRecord.OutboxStatus.PENDING;

                transactionTemplate.executeWithoutResult(status ->
                        updateEventStatus(event, nextStatus, ex.getMessage())
                );
            }
        }
    }

    @Scheduled(fixedDelayString = "120000")
    public void recoverZombieEvents() {
        transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        transactionTemplate.executeWithoutResult(status -> {
            Instant deathThreshold = Instant.now().minus(5, ChronoUnit.MINUTES);

            // 🧠 160 IQ FIX 3: Signature Compilation Match
            // We pass the explicit Enums to satisfy Hibernate's strict JPQL bindings
            int recovered = outboxRepository.resetStuckProcessingEvents(
                    deathThreshold,
                    OutboxRecord.OutboxStatus.PENDING,
                    OutboxRecord.OutboxStatus.PROCESSING
            );

            if (recovered > 0) {
                log.warn("Security Matrix: Sweeper Protocol activated. Recovered {} zombie events.", recovered);
            }
        });
    }

    private void updateEventStatus(OutboxRecord event, OutboxRecord.OutboxStatus status, String errorMessage) {
        event.setStatus(status);

        // Only set processedAt if it was actually processed successfully or completely failed
        if (status == OutboxRecord.OutboxStatus.PROCESSED || status == OutboxRecord.OutboxStatus.FAILED) {
            event.setProcessedAt(Instant.now());
        }

        // 🧠 160 IQ FIX 4: Domain-Driven Encapsulation Hand-off
        // Removed the ugly ternary operator `errorMessage.length() > 950 ? ...`
        // The Entity itself now enforces database constraints natively.
        event.setErrorMessage(errorMessage);

        outboxRepository.save(event);
    }

    private void dispatchNetworkCall(OutboxRecord event) {
        switch (event.getEventType()) {
            case "APPLICATION_SUBMITTED_EMAIL":
                log.info("Dispatch Target Secured: Sending Application Submission Email for {}", event.getAggregateId());
                break;
            case "ELIGIBILITY_APPROVED_SMS":
                log.info("Dispatch Target Secured: Sending Approval SMS for {}", event.getAggregateId());
                break;
            default:
                log.warn("Security Alert: Unknown event type encountered in Outbox: {}", event.getEventType());
        }
    }
}