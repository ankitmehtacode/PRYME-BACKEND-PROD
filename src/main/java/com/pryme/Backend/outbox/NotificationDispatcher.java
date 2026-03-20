package com.pryme.Backend.outbox;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
     * 🧠 THE DISPATCH ENGINE
     * Runs every 5 seconds. Pulls a batch of 50 events using SKIP LOCKED.
     * Operates completely independently of the main user threads.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        // 1. Safely acquire a locked batch of pending events
        List<OutboxRecord> pendingEvents = outboxRepository.fetchPendingEventsForProcessing(50);

        if (pendingEvents.isEmpty()) {
            return; // Engine idle, CPU conserved.
        }

        log.debug("Outbox Engine: Processing batch of {} pending events...", pendingEvents.size());

        for (OutboxRecord event : pendingEvents) {
            try {
                // 2. Route the event to the correct external API
                dispatch(event);

                // 3. Mark as successfully processed
                event.setStatus(OutboxRecord.OutboxStatus.PROCESSED);
                event.setProcessedAt(Instant.now());
                event.setErrorMessage(null);

            } catch (Exception ex) {
                // 🧠 ERROR CONTAINMENT: A failed email won't crash the whole batch
                log.error("Outbox Engine: Failed to dispatch event ID {}", event.getId(), ex);
                event.setStatus(OutboxRecord.OutboxStatus.FAILED);
                event.setErrorMessage(ex.getMessage() != null ? ex.getMessage() : "Unknown Dispatch Exception");
            }
        }

        // 4. Batch update all statuses to PostgreSQL instantly
        outboxRepository.saveAll(pendingEvents);
    }

    private void dispatch(OutboxRecord event) {
        switch (event.getEventType()) {
            case "APPLICATION_SUBMITTED_EMAIL":
                // emailService.sendFromCloudflareEdge(event.getPayload());
                log.info("Mock Dispatch: Sending Application Submission Email for {}", event.getAggregateId());
                break;
            case "ELIGIBILITY_APPROVED_SMS":
                // smsService.send(event.getPayload());
                log.info("Mock Dispatch: Sending Approval SMS for {}", event.getAggregateId());
                break;
            default:
                log.warn("Unknown event type encountered in Outbox: {}", event.getEventType());
        }
    }
}