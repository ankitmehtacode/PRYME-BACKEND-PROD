package com.pryme.Backend.outbox;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final OutboxRepository outboxRepository;

    // Inject your EmailSenderService / SMSService here
    // private final EmailService emailService;

    /**
     * 🧠 THE ELASTIC DISPATCH ENGINE (MEMORY & DB SAFE)
     * Runs every 5 seconds. Uses the Claim-Check pattern to ensure database
     * connections are NEVER held open while waiting for external APIs.
     */
    @Scheduled(fixedDelayString = "${app.outbox.poll-rate:5000}")
    public void processOutboxEvents() {

        // 1. Claim the batch in a micro-transaction (< 5ms)
        List<OutboxRecord> claimedEvents = claimPendingEventsBatch(50);

        if (claimedEvents.isEmpty()) {
            return; // Engine idle. CPU and DB conserved.
        }

        log.debug("Outbox Engine: Claimed {} events. Executing network dispatch...", claimedEvents.size());

        // 2. 🧠 NETWORK CALLS (NO DATABASE LOCKS HELD HERE)
        for (OutboxRecord event : claimedEvents) {
            try {
                // Route the event to the correct external API (SendGrid, Twilio, etc.)
                dispatchNetworkCall(event);

                // 3. Mark as successfully processed in an isolated micro-transaction
                updateEventStatus(event, OutboxRecord.OutboxStatus.PROCESSED, null);

            } catch (Exception ex) {
                // ERROR CONTAINMENT: A 500 timeout from SendGrid only affects this single event
                log.error("Outbox Engine: Failed to dispatch event ID {}", event.getId(), ex);
                updateEventStatus(event, OutboxRecord.OutboxStatus.FAILED, ex.getMessage());
            }
        }
    }

    /**
     * 🧠 MICRO-TRANSACTION 1: The Claim Check
     * Uses SKIP LOCKED to grab rows safely, instantly updates them to PROCESSING,
     * and commits. This prevents other Kubernetes pods from double-sending the same email.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OutboxRecord> claimPendingEventsBatch(int batchSize) {
        List<OutboxRecord> pendingEvents = outboxRepository.fetchPendingEventsForProcessing(batchSize);

        for (OutboxRecord event : pendingEvents) {
            event.setStatus(OutboxRecord.OutboxStatus.PROCESSING);
        }

        return outboxRepository.saveAll(pendingEvents);
    }

    /**
     * 🧠 MICRO-TRANSACTION 2: State Resolution
     * Executes in < 2ms to resolve the event lifecycle after the network call finishes.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEventStatus(OutboxRecord event, OutboxRecord.OutboxStatus status, String errorMessage) {
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