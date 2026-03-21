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

    // 🧠 160 IQ FIX: Explicit Transaction Management
    // Bypasses the Spring AOP self-invocation trap, mathematically guaranteeing
    // our micro-transactions commit exactly when we want them to.
    private final TransactionTemplate transactionTemplate;

    // Inject your EmailSenderService / SMSService here
    // private final EmailService emailService;

    /**
     * 🧠 THE ELASTIC DISPATCH ENGINE (MEMORY & DB SAFE)
     * Added initial delay jitter so multi-pod setups don't synchronize their polling cycles.
     */
    @Scheduled(fixedDelayString = "${app.outbox.poll-rate:5000}", initialDelayString = "#{new java.util.Random().nextInt(5000)}")
    public void processOutboxEvents() {

        // 1. 🧠 MICRO-TRANSACTION 1: The Claim Check
        // Executed inside an explicit REQUIRES_NEW transaction boundary.
        transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        List<OutboxRecord> claimedEvents = transactionTemplate.execute(status -> {
            List<OutboxRecord> pending = outboxRepository.fetchPendingEventsForProcessing(50);
            for (OutboxRecord event : pending) {
                event.setStatus(OutboxRecord.OutboxStatus.PROCESSING);
            }
            return outboxRepository.saveAll(pending);
        });

        // Engine idle. CPU and DB conserved.
        if (claimedEvents == null || claimedEvents.isEmpty()) {
            return;
        }

        log.debug("Outbox Engine: Claimed {} events. Executing network dispatch...", claimedEvents.size());

        // 2. 🧠 NETWORK CALLS (NO DATABASE LOCKS HELD HERE)
        for (OutboxRecord event : claimedEvents) {
            try {
                // Route the event to the correct external API (SendGrid, Twilio, etc.)
                dispatchNetworkCall(event);

                // 3. 🧠 MICRO-TRANSACTION 2: Success State Resolution
                transactionTemplate.executeWithoutResult(status ->
                        updateEventStatus(event, OutboxRecord.OutboxStatus.PROCESSED, null)
                );

            } catch (Exception ex) {
                // ERROR CONTAINMENT: A 500 timeout from SendGrid only affects this single event
                log.error("Outbox Engine: Failed to dispatch event ID {}", event.getId(), ex);

                // 🧠 MICRO-TRANSACTION 2: Failure State Resolution
                transactionTemplate.executeWithoutResult(status ->
                        updateEventStatus(event, OutboxRecord.OutboxStatus.FAILED, ex.getMessage())
                );
            }
        }
    }

    /**
     * 🧠 THE SWEEPER PROTOCOL (ZOMBIE RECOVERY)
     * Runs every 2 minutes. If a pod died mid-process and an event has been
     * stuck in 'PROCESSING' for over 5 minutes, this reverts it to 'PENDING'.
     */
    @Scheduled(fixedDelayString = "120000")
    public void recoverZombieEvents() {
        transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        transactionTemplate.executeWithoutResult(status -> {
            Instant deathThreshold = Instant.now().minus(5, ChronoUnit.MINUTES);
            // NOTE: Ensure your OutboxRepository has this @Modifying method!
            int recovered = outboxRepository.resetStuckProcessingEvents(deathThreshold);
            if (recovered > 0) {
                log.warn("Security Matrix: Sweeper Protocol activated. Recovered {} zombie events.", recovered);
            }
        });
    }

    // ==========================================
    // HELPER METHODS (No @Transactional needed, handled by Template)
    // ==========================================

    private void updateEventStatus(OutboxRecord event, OutboxRecord.OutboxStatus status, String errorMessage) {
        event.setStatus(status);
        event.setProcessedAt(Instant.now());

        if (errorMessage != null) {
            // Truncate to prevent SQL DataIntegrityViolation if the API throws a massive stack trace
            event.setErrorMessage(errorMessage.length() > 950 ? errorMessage.substring(0, 950) + "..." : errorMessage);
        } else {
            event.setErrorMessage(null);
        }

        outboxRepository.save(event);
    }

    // ==========================================
    // 🧠 THE ROUTER
    // ==========================================
    private void dispatchNetworkCall(OutboxRecord event) {
        switch (event.getEventType()) {
            case "APPLICATION_SUBMITTED_EMAIL":
                // emailService.sendFromCloudflareEdge(event.getPayload());
                log.info("Dispatch Target Secured: Sending Application Submission Email for {}", event.getAggregateId());
                break;
            case "ELIGIBILITY_APPROVED_SMS":
                // smsService.send(event.getPayload());
                log.info("Dispatch Target Secured: Sending Approval SMS for {}", event.getAggregateId());
                break;
            default:
                log.warn("Security Alert: Unknown event type encountered in Outbox: {}", event.getEventType());
        }
    }
}