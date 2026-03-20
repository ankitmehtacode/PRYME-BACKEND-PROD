package com.pryme.Backend.crm;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.crm.dto.InitialLeadCaptureRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationService applicationService;

    // ==========================================
    // 🧠 FAILPROOF PRINCIPAL EXTRACTOR
    // ==========================================
    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ForbiddenException("Security matrix violation: Authentication required.");
        }
        try {
            return auth.getPrincipal() instanceof UUID ?
                    (UUID) auth.getPrincipal() :
                    UUID.fromString(auth.getPrincipal().toString());
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("Invalid security token footprint.");
        }
    }

    // ==========================================
    // 🧠 PHASE 2: PROGRESSIVE LEAD CAPTURE (STAGE 1)
    // ==========================================
    @PostMapping("/initial-intake")
    public ResponseEntity<Map<String, Object>> captureInitialLead(
            @Valid @RequestBody InitialLeadCaptureRequest request,
            Authentication authentication) {

        UUID userId = extractUserId(authentication);
        log.info("Lead Capture Gateway: Ingesting Stage 1 KYC for User: {}", userId);

        LoanApplication app = applicationService.captureInitialLead(userId, request);

        // We return the secure Application ID so React can instantly route the user
        // to the Bank Selection Interstitial and use this ID for the Vault later.
        return ResponseEntity.ok(Map.of(
                "status", "SECURED",
                "applicationId", app.getApplicationId(),
                "message", "Lead successfully captured and securely vaulted."
        ));
    }

    // ==========================================
    // 🧠 PHASE 3: DEEP PROFILING & BANK SELECTION
    // ==========================================
    // This dynamically catches the Zustand JSON payload and merges it safely into the database.
    @PatchMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> updateProgress(
            @PathVariable String applicationId,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {

        // Security check ensures the user is logged in before altering the matrix
        extractUserId(authentication);

        return ResponseEntity.ok(applicationService.updateProgress(applicationId, updates));
    }

    // ==========================================
    // 🧠 PHASE 4: STATE TRANSITION ENGINE
    // ==========================================
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable String applicationId,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication) {

        extractUserId(authentication);
        return ResponseEntity.ok(applicationService.updateStatus(applicationId, request));
    }

    @PatchMapping("/{applicationId}/assign")
    public ResponseEntity<ApplicationResponse> assignLead(
            @PathVariable String applicationId,
            @Valid @RequestBody AssignLeadRequest request,
            Authentication authentication) {

        // Note: Global Method Security or URL-based config should restrict this to ADMIN only
        extractUserId(authentication);
        return ResponseEntity.ok(applicationService.assign(applicationId, request));
    }

    // ==========================================
    // DASHBOARD RETRIEVAL
    // ==========================================
    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(applicationService.listMyApplications(userId));
    }

    // Admin endpoint to view the entire matrix
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(Authentication authentication) {
        extractUserId(authentication);
        return ResponseEntity.ok(applicationService.listApplications());
    }
}}