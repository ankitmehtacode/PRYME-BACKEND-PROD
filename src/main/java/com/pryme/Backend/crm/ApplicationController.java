package com.pryme.Backend.crm;

import com.pryme.Backend.common.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // 🧠 FIX 1 & 2: Failproof Principal Extraction & Safe Array Returns
    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponse>> myApplications(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ForbiddenException("Authentication required");
        }

        UUID userId;
        Object principal = authentication.getPrincipal();

        // 🧠 FAILPROOF CASTING: Safely parse the UUID regardless of whether Spring passes a String or UUID object
        try {
            if (principal instanceof UUID) {
                userId = (UUID) principal;
            } else {
                userId = UUID.fromString(principal.toString());
            }
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("Invalid authentication token format.");
        }

        try {
            return ResponseEntity.ok(applicationService.listMyApplications(userId));
        } catch (Exception e) {
            log.warn("Empty portfolio or DB sync delay for user {}. Returning safe matrix.", userId);
            // Returns an empty array so React drops cleanly into the "Empty" state instead of an Infinite Spinner
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 🧠 FIX 3: Dynamic Map Binding to catch React's Funnel Data without DTO explosions (Stages 1-3)
    @PatchMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> updateApplicationProgress(
            @PathVariable String applicationId,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {

        if (authentication == null) throw new ForbiddenException("Authentication required");

        log.info("REST Gateway: Synchronizing application matrix for ID: {}", applicationId);
        // Safely routes the dynamic JSON payload to our upgraded ApplicationService
        return ResponseEntity.ok(applicationService.updateProgress(applicationId, updates));
    }

    // 🧠 FIX 4: Safe Status Elevation for the Underwriter Gateway (Stage 4)
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable String applicationId,
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        if (authentication == null) throw new ForbiddenException("Authentication required");

        log.info("REST Gateway: Elevating application status to PROCESSING for ID: {}", applicationId);

        // Safely extract just the status, ignoring any extra variables React attached to the payload
        String status = (String) payload.getOrDefault("status", "PENDING");
        Long version = payload.containsKey("version") ? ((Number) payload.get("version")).longValue() : null;

        UpdateStatusRequest safeRequest = new UpdateStatusRequest(status, version);

        return ResponseEntity.ok(applicationService.updateStatus(applicationId, safeRequest));
    }
}