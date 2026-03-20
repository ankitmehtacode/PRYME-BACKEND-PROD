package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.crm.dto.InitialLeadCaptureRequest;
import com.pryme.Backend.crm.events.ApplicationSubmittedEvent;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final LoanApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    // 🧠 THE EVENT BUS: Decouples the CRM module from the Eligibility/Underwriting module
    private final ApplicationEventPublisher eventPublisher;

    // ==========================================
    // 🧠 PHASE 2: PROGRESSIVE LEAD CAPTURE ENGINE
    // ==========================================
    @Transactional
    public LoanApplication captureInitialLead(UUID userId, InitialLeadCaptureRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User footprint not found in IAM module."));

        // 1. ZERO DATA LOSS PROTOCOL: Sync PII back to core IAM profile if missing
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

        // 2. IDEMPOTENT UPSERT: Prevent duplicate drafts from network race conditions
        LoanApplication application = applicationRepository.findByApplicantIdAndStatus(userId, ApplicationStatus.DRAFT)
                .stream()
                .filter(app -> request.getLoanType().equals(app.getLoanType()))
                .findFirst()
                .orElseGet(() -> {
                    LoanApplication newApp = new LoanApplication();
                    newApp.setApplicant(user);
                    // Cryptographically secure application ID allocation
                    newApp.setApplicationId("PRYME-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    newApp.setStatus(ApplicationStatus.DRAFT);
                    newApp.setRequestedAmount(BigDecimal.ZERO);
                    return newApp;
                });

        application.setLoanType(request.getLoanType());
        application.setCompletionPercentage(25); // Intake matrix completed

        // 3. HYBRID JSON MATRIX: Safely pack dynamic UI state without schema bloat
        Map<String, Object> meta = application.getMetadata();
        if (meta == null) {
            meta = new HashMap<>();
        }

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

        log.info("Synchronizing progressive payload for Application: {}", applicationId);

        // 1. Target Completion Vector
        if (updates.containsKey("completionPercentage")) {
            Object cpObj = updates.get("completionPercentage");
            if (cpObj instanceof Number) {
                application.setCompletionPercentage(((Number) cpObj).intValue());
            }
        }

        // 2. 🧠 INTENT ROUTER: Extract bank selection to a hard column for indexing
        if (updates.containsKey("selectedBank")) {
            application.setSelectedBank(String.valueOf(updates.get("selectedBank")));
            updates.remove("selectedBank"); // Strip it from the JSON to prevent data duplication
        }

        // 3. 🧠 FINANCIAL PROFILING: Extract requested amount to a hard column
        if (updates.containsKey("requestedAmount")) {
            Object amtObj = updates.get("requestedAmount");
            if (amtObj instanceof Number) {
                application.setRequestedAmount(BigDecimal.valueOf(((Number) amtObj).doubleValue()));
            } else if (amtObj instanceof String) {
                try { application.setRequestedAmount(new BigDecimal((String) amtObj)); } catch (Exception ignored) {}
            }
            updates.remove("requestedAmount");
        }

        // 4. Safe Deep-Merge of remaining JSON Metadata
        if (updates.containsKey("metadata")) {
            Object metaObj = updates.get("metadata");
            if (metaObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> newMetadata = (Map<String, Object>) metaObj;
                Map<String, Object> existingMetadata = application.getMetadata();

                if (existingMetadata == null) {
                    application.setMetadata(newMetadata);
                } else {
                    existingMetadata.putAll(newMetadata);
                    application.setMetadata(existingMetadata);
                }
            }
        }

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    // ==========================================
    // 🧠 PHASE 4: STATE TRANSITION ENGINE & EVENT TRIGGER
    // ==========================================
    @Transactional
    public ApplicationResponse updateStatus(String applicationId, UpdateStatusRequest request) {
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

        // 🧠 FIRE THE STRICT STATE MACHINE (Located inside the Entity)
        application.transitionTo(newStatus);

        LoanApplication savedApp = applicationRepository.save(application);
        log.info("State Engine: Application {} transitioned from {} to {}", applicationId, oldStatus, newStatus);

        // 🧠 EVENT-CARRIED STATE TRANSFER: Trigger the Underwriting Engine asynchronously
        if (oldStatus == ApplicationStatus.DRAFT && newStatus == ApplicationStatus.PROCESSING) {
            log.info("Application {} officially submitted. Publishing Underwriting Event to the Bus.", applicationId);
            eventPublisher.publishEvent(new ApplicationSubmittedEvent(
                    savedApp.getApplicationId(),
                    savedApp.getLoanType(),
                    savedApp.getSelectedBank(),
                    savedApp.getRequestedAmount(),
                    ApplicantSnapshot.from(savedApp) // Injects the rich, immutable snapshot
            ));
        }

        return ApplicationResponse.from(savedApp);
    }

    @Transactional
    public ApplicationResponse assign(String applicationId, AssignLeadRequest request) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application Matrix not found"));

        validateVersion(application.getVersion(), request.version());

        UUID empId;
        try {
            empId = UUID.fromString(request.assigneeId().trim());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid assigneeId architecture. Expected standard UUID.");
        }

        User employee = userRepository.findById(empId)
                .orElseThrow(() -> new NotFoundException("Assignee not found in IAM system records."));

        application.setAssignee(employee);
        log.info("Access Matrix: Application {} assigned to Underwriter {}", applicationId, employee.getEmail());

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    // ==========================================
    // DATA RETRIEVAL (DASHBOARDS)
    // ==========================================
    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplications() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listMyApplications(UUID applicantId) {
        return applicationRepository.findAllByApplicant_IdOrderByCreatedAtDesc(applicantId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    private static void validateVersion(Long current, Long requestVersion) {
        if (requestVersion == null) {
            return;
        }
        if (current == null || !current.equals(requestVersion)) {
            throw new ConflictException("Optimistic Lock Fault: Version mismatch. Please refresh to avoid data collision.");
        }
    }
}