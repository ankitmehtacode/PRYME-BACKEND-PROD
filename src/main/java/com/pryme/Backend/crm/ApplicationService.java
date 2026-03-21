package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.crm.dto.InitialLeadCaptureRequest;
import com.pryme.Backend.crm.events.ApplicationSubmittedEvent;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import com.pryme.Backend.outbox.OutboxRecord;
import com.pryme.Backend.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final LoanApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    // 🧠 THE INTEGRATION ENGINES
    private final ApplicationEventPublisher eventPublisher;      // For internal micro-domain routing
    private final OutboxRepository outboxRepository;             // For guaranteed external notifications
    private final ApplicationStatusHistoryRepository historyRepository; // For strict RBI/Financial Compliance

    // ==========================================
    // 🧠 PHASE 2: PROGRESSIVE LEAD CAPTURE ENGINE
    // ==========================================
    @Transactional
    public LoanApplication captureInitialLead(UUID userId, InitialLeadCaptureRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User footprint not found in IAM module."));

        boolean userUpdated = false;
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getMobileNumber());
            userUpdated = true;
        }
        if (user.getCity() == null || user.getCity().isBlank()) {
            user.setCity(request.getCity());
            userUpdated = true;
        }
        if (user.getState() == null || user.getState().isBlank()) {
            user.setState(request.getState());
            userUpdated = true;
        }
        if (userUpdated) {
            userRepository.save(user);
        }

        LoanApplication application = applicationRepository.findByApplicantIdAndStatus(userId, ApplicationStatus.DRAFT)
                .stream()
                .filter(app -> request.getLoanType().equals(app.getLoanType()))
                .findFirst()
                .orElseGet(() -> {
                    LoanApplication newApp = new LoanApplication();
                    newApp.setApplicant(user);
                    newApp.setApplicationId("PRYME-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    newApp.setStatus(ApplicationStatus.DRAFT);
                    newApp.setRequestedAmount(BigDecimal.ZERO);
                    return newApp;
                });

        application.setLoanType(request.getLoanType());
        application.setCompletionPercentage(25);

        Map<String, Object> meta = application.getMetadata() != null ? application.getMetadata() : new HashMap<>();
        meta.put("dob", request.getDob());
        meta.put("pinCode", request.getPinCode());
        meta.put("employmentType", request.getEmploymentType());

        if (request.getSalariedSubType() != null) meta.put("salariedSubType", request.getSalariedSubType());
        if (request.getProfessionalSubType() != null) meta.put("professionalSubType", request.getProfessionalSubType());
        if (request.getBusinessSubType() != null) meta.put("businessSubType", request.getBusinessSubType());

        application.setMetadata(meta);

        log.info("Lead Capture Engine: Stage 1 Secured for Application {}", application.getApplicationId());
        return applicationRepository.save(application);
    }

    // ==========================================
    // 🧠 PHASE 3: DEEP PROFILING & INTENT ROUTING
    // ==========================================
    @Transactional
    public ApplicationResponse updateProgress(String applicationId, Map<String, Object> updates) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application Matrix not found"));

        if (updates.containsKey("completionPercentage")) {
            application.setCompletionPercentage(((Number) updates.get("completionPercentage")).intValue());
        }

        if (updates.containsKey("selectedBank")) {
            application.setSelectedBank(String.valueOf(updates.get("selectedBank")));
            updates.remove("selectedBank");
        }

        if (updates.containsKey("requestedAmount")) {
            Object amtObj = updates.get("requestedAmount");
            if (amtObj instanceof Number) {
                application.setRequestedAmount(BigDecimal.valueOf(((Number) amtObj).doubleValue()));
            } else if (amtObj instanceof String) {
                try { application.setRequestedAmount(new BigDecimal((String) amtObj)); } catch (Exception ignored) {}
            }
            updates.remove("requestedAmount");
        }

        if (updates.containsKey("metadata")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> newMetadata = (Map<String, Object>) updates.get("metadata");
            Map<String, Object> existingMetadata = application.getMetadata() != null ? application.getMetadata() : new HashMap<>();
            existingMetadata.putAll(newMetadata);
            application.setMetadata(existingMetadata);
        }

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    // ==========================================
    // 🧠 PHASE 4 & 6: STATE TRANSITION, OUTBOX, AND AUDIT LEDGER
    // ==========================================
    @Transactional
    public ApplicationResponse updateStatus(String applicationId, UpdateStatusRequest request, UUID actorId) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application Matrix not found"));

        validateVersion(application.getVersion(), request.version());

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid status transition requested.");
        }

        ApplicationStatus oldStatus = application.getStatus();

        // 1. Strict Domain Entity State Transition
        application.transitionTo(newStatus);
        LoanApplication savedApp = applicationRepository.save(application);

        // 2. 🧠 APPEND-ONLY AUDIT LEDGER (Compliance)
        historyRepository.save(ApplicationStatusHistory.builder()
                .applicationId(savedApp.getApplicationId())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .changedBy(actorId)
                .changedAt(Instant.now())
                .build());

        // 3. 🧠 TRANSACTIONAL OUTBOX (Guaranteed External Delivery)
        // Uses Java 17 Text Blocks for clean JSON construction
        if (newStatus == ApplicationStatus.PROCESSING || newStatus == ApplicationStatus.APPROVED) {
            String payload = """
                    {
                        "applicationId": "%s",
                        "applicantName": "%s",
                        "status": "%s"
                    }
                    """.formatted(savedApp.getApplicationId(), savedApp.getApplicant().getFullName(), newStatus.name());

            outboxRepository.save(OutboxRecord.builder()
                    .aggregateType("LOAN_APPLICATION")
                    .aggregateId(savedApp.getApplicationId())
                    .eventType("APPLICATION_STATUS_UPDATE_EMAIL")
                    .payload(payload)
                    .build());
        }

        // 4. 🧠 EVENT-CARRIED STATE TRANSFER (Internal Async Triggers)
        if (oldStatus == ApplicationStatus.DRAFT && newStatus == ApplicationStatus.PROCESSING) {
            log.info("Application {} submitted. Publishing Underwriting Event to the Bus.", applicationId);
            eventPublisher.publishEvent(new ApplicationSubmittedEvent(
                    savedApp.getApplicationId(),
                    savedApp.getLoanType(),
                    savedApp.getSelectedBank(),
                    savedApp.getRequestedAmount(),
                    ApplicantSnapshot.from(savedApp)
            ));
        }

        return ApplicationResponse.from(savedApp);
    }

    @Transactional
    public ApplicationResponse assign(String applicationId, AssignLeadRequest request, UUID adminId) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application Matrix not found"));

        validateVersion(application.getVersion(), request.version());

        UUID empId;
        try {
            empId = UUID.fromString(request.assigneeId().trim());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid assigneeId architecture.");
        }

        User employee = userRepository.findById(empId)
                .orElseThrow(() -> new NotFoundException("Assignee not found in IAM system records."));

        application.setAssignee(employee);

        // 🧠 AUDIT LEDGER: Log assignment
        historyRepository.save(ApplicationStatusHistory.builder()
                .applicationId(application.getApplicationId())
                .oldStatus(application.getStatus().name())
                .newStatus(application.getStatus().name())
                .changedBy(adminId)
                .changedAt(Instant.now())
                .build());

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    // ==========================================
    // 🧠 DATA RETRIEVAL (OOM PREVENTION VIA PAGINATION)
    // ==========================================
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(Pageable pageable) {
        // Prevents OutOfMemoryErrors by only loading requested slice of data
        return applicationRepository.findAll(pageable)
                .map(ApplicationResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listMyApplications(UUID applicantId, Pageable pageable) {
        return applicationRepository.findAllByApplicant_Id(applicantId, pageable)
                .map(ApplicationResponse::from);
    }

    private static void validateVersion(Long current, Long requestVersion) {
        if (requestVersion == null) return;
        if (current == null || !current.equals(requestVersion)) {
            throw new ConflictException("Optimistic Lock Fault: Version mismatch. Please refresh to avoid data collision.");
        }
    }
}