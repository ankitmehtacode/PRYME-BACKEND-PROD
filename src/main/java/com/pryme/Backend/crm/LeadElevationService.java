package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import com.pryme.Backend.iam.UserIdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
public class LeadElevationService {

    private static final Logger log = LoggerFactory.getLogger(LeadElevationService.class);

    /**
     * 🧠 TEMPORAL GUARD: Anonymous leads older than 24 hours cannot be elevated.
     * This prevents stale lead harvesting attacks where an attacker collects
     * Lead UUIDs over time and bulk-claims them later.
     */
    private static final int LEAD_MAX_AGE_HOURS = 24;

    private final LeadRepository leadRepository;
    private final LoanApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserIdGeneratorService userIdGeneratorService;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Transactional
    public ApplicationResponse elevate(UUID leadId, UUID userId, String selectedBank) {

        // 1. Validate the public lead exists
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead identity not found in the public matrix."));

        // 2. 🧠 ZERO-COMPROMISE SECURITY: Prevent double-elevation attacks
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new ConflictException("Security Violation: This lead has already been converted to an active application.");
        }

        // 3. 🧠 TEMPORAL VALIDATION: Reject stale leads to prevent harvesting
        if (lead.getCreatedAt() != null &&
                lead.getCreatedAt().isBefore(LocalDateTime.now().minusHours(LEAD_MAX_AGE_HOURS))) {
            throw new ForbiddenException(
                    "This lead has expired. Anonymous leads must be elevated within " + LEAD_MAX_AGE_HOURS + " hours.");
        }

        // 4. Validate the authenticated user exists
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Authenticated User identity not found. Cannot bind application."));

        // 5. 🧠 CONCURRENCY-SAFE APPLICATION ID
        // Previous implementation used count() which is NOT safe under concurrent writes.
        // Two threads calling count() at the same instant get the same number → duplicate IDs.
        // UUID-based generation is cryptographically guaranteed to be unique.
        String generateAppId = "PRYME-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, Object> metadataMap = new HashMap<>();
        if (lead.getMetadata() != null && !lead.getMetadata().isBlank()) {
            try {
                metadataMap = JSON_MAPPER.readValue(lead.getMetadata(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                // Ignore parsing errors, default to empty map
            }
        }

        // 🧠 PIPELINE FIX: Inject top-level Lead fields into metadata for Admin Dashboard visibility.
        // These fields are stored as columns on the Lead entity but were never forwarded to the
        // LoanApplication.metadata JSONB column, causing them to appear empty in the admin panel.
        if (lead.getCibilScore() != null) metadataMap.putIfAbsent("cibilScore", lead.getCibilScore());
        metadataMap.putIfAbsent("phone", lead.getPhone());
        metadataMap.putIfAbsent("userName", lead.getUserName());
        if (lead.getLoanAmount() != null) metadataMap.putIfAbsent("loanAmount", lead.getLoanAmount());

        // 5.5 🧠 PIPELINE DATA INTEGRITY FIX: Backfill User with Lead Data
        boolean userUpdated = false;
        
        // Fix Name (if missing or just an email prefix from Google Auth)
        String currentName = applicant.getFullName();
        if (currentName == null || currentName.trim().isEmpty() || currentName.equals("Guest") || 
            (applicant.getEmail() != null && currentName.equals(applicant.getEmail().split("@")[0]))) {
            if (lead.getUserName() != null && !lead.getUserName().isBlank()) {
                applicant.setFullName(lead.getUserName());
                userUpdated = true;
            }
        }
        
        // Fix Phone
        if (applicant.getPhone() == null || applicant.getPhone().trim().isEmpty()) {
            if (lead.getPhone() != null && !lead.getPhone().isBlank()) {
                applicant.setPhone(lead.getPhone());
                userUpdated = true;
            }
        }
        
        // Fix City & State
        if (metadataMap.containsKey("city") && (applicant.getCity() == null || applicant.getCity().trim().isEmpty())) {
            applicant.setCity((String) metadataMap.get("city"));
            userUpdated = true;
        }
        if (metadataMap.containsKey("state") && (applicant.getState() == null || applicant.getState().trim().isEmpty())) {
            applicant.setState((String) metadataMap.get("state"));
            userUpdated = true;
        }
        
        // Sync full metadata to User for ApplicantSnapshot
        Map<String, Object> userMetadata = applicant.getMetadata();
        if (userMetadata == null) {
            userMetadata = new HashMap<>();
        }
        
        boolean metadataUpdated = false;
        for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
            if (!userMetadata.containsKey(entry.getKey()) || userMetadata.get(entry.getKey()) == null) {
                userMetadata.put(entry.getKey(), entry.getValue());
                metadataUpdated = true;
            }
        }
        
        if (metadataUpdated) {
            applicant.setMetadata(userMetadata);
            userUpdated = true;
        }
        
        userIdGeneratorService.ensureUserIds(applicant);
        applicant = userRepository.save(applicant);
        log.info("🔄 Ensured custom IDs and backfilled User {} metadata from Lead {}", userId, leadId);

        // 6. Fuse the data into the highly-secure LoanApplication entity
        LoanApplication application = LoanApplication.builder()
                .applicationId(generateAppId)
                .applicant(applicant)
                .loanType(lead.getLoanType())
                .requestedAmount(lead.getLoanAmount())
                .declaredCibilScore(lead.getCibilScore() != null ? lead.getCibilScore() : -1)
                .selectedBank(selectedBank)
                .metadata(metadataMap)
                .status(ApplicationStatus.SUBMITTED) // Places it natively into the Admin Kanban board
                .build();

        application = applicationRepository.save(application);

        // 7. Lock the public lead so it cannot be tampered with again
        lead.setStatus(LeadStatus.CONVERTED);
        leadRepository.save(lead);

        log.info("🔒 Lead {} securely elevated to Application {} by User {}",
                leadId, generateAppId, userId);

        return ApplicationResponse.from(application);
    }
}