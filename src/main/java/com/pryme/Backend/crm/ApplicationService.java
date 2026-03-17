package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final LoanApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplications() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream().map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listMyApplications(UUID applicantId) {
        return applicationRepository.findAllByApplicant_IdOrderByCreatedAtDesc(applicantId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    // 🧠 SILICON-VALLEY FIX: Dynamic Funnel Progression Engine
    // Gracefully accepts React's optimistic Map payloads and safely merges them into the DB
    // This guarantees no data is lost when a user clicks "Save & Continue" between stages.
    @Transactional
    public ApplicationResponse updateProgress(String applicationId, Map<String, Object> updates) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        log.info("Synchronizing progressive matrix for Application: {}", applicationId);

        // 1. Safely extract and update the completion progress bar
        if (updates.containsKey("completionPercentage")) {
            Object cpObj = updates.get("completionPercentage");
            if (cpObj instanceof Number) {
                application.setCompletionPercentage(((Number) cpObj).intValue());
            }
        }

        // 2. Safely extract and deep-merge the dynamic metadata (KYC, Financials, etc.)
        if (updates.containsKey("metadata")) {
            Object metaObj = updates.get("metadata");
            if (metaObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> newMetadata = (Map<String, Object>) metaObj;
                Map<String, Object> existingMetadata = application.getMetadata();

                if (existingMetadata == null) {
                    application.setMetadata(newMetadata);
                } else {
                    // We merge instead of overwrite, so Stage 2 doesn't delete Stage 1's data!
                    existingMetadata.putAll(newMetadata);
                    application.setMetadata(existingMetadata);
                }
            }
        }

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse updateStatus(String applicationId, UpdateStatusRequest request) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validateVersion(application.getVersion(), request.version());

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid status value");
        }

        application.setStatus(newStatus);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse assign(String applicationId, AssignLeadRequest request) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validateVersion(application.getVersion(), request.version());

        // 🧠 PRODUCTION FIX: Safely parse the Assignee ID and fetch the actual Relational User Entity
        UUID empId;
        try {
            empId = UUID.fromString(request.assigneeId().trim());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid assigneeId format. Expected a valid User UUID.");
        }

        User employee = userRepository.findById(empId)
                .orElseThrow(() -> new NotFoundException("Assignee not found in IAM system records."));

        application.setAssignee(employee);

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    private static void validateVersion(Long current, Long requestVersion) {
        if (requestVersion == null) {
            return;
        }
        if (current == null || !current.equals(requestVersion)) {
            throw new ConflictException("Version mismatch. Please refresh and retry.");
        }
    }
}