package com.pryme.Backend.crm;

import com.pryme.Backend.common.ForbiddenException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
// 🧠 STRICT RBAC GATEWAY: Mathematically guarantees no standard user can access these endpoints
// even if the global SecurityConfig filter chain is accidentally misconfigured.
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final ApplicationService applicationService;

    // ==========================================
    // 🧠 FAILPROOF PRINCIPAL EXTRACTOR (AUDIT TRAIL ENGINE)
    // ==========================================
    private UUID extractAdminId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ForbiddenException("Security matrix violation: Admin authentication required.");
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
    // 1. MASTER DASHBOARD TELEMETRY
    // ==========================================
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(Authentication authentication) {
        UUID adminId = extractAdminId(authentication);
        log.info("Audit Trail: Admin/Underwriter {} requested the Master Application Matrix.", adminId);

        return ResponseEntity.ok(applicationService.listApplications());
    }

    // ==========================================
    // 2. STATE TRANSITION ENGINE (APPROVE / REJECT)
    // ==========================================
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable String applicationId,
            @Valid @RequestBody UpdateStatusRequest payload,
            Authentication authentication
    ) {
        UUID adminId = extractAdminId(authentication);
        log.warn("Audit Trail: Admin {} initiating status transition to [{}] for Application {}",
                adminId, payload.status(), applicationId);

        ApplicationResponse updated = applicationService.updateStatus(applicationId, payload);

        return ResponseEntity.ok(Map.of(
                "status", "TRANSITION_SUCCESS",
                "message", "Application state transition executed and secured.",
                "application", updated
        ));
    }

    // ==========================================
    // 3. ASSIGNMENT ROUTING ENGINE
    // ==========================================
    @PatchMapping("/{applicationId}/assign")
    public ResponseEntity<Map<String, Object>> assignApplication(
            @PathVariable String applicationId,
            @Valid @RequestBody AssignLeadRequest payload,
            Authentication authentication
    ) {
        UUID adminId = extractAdminId(authentication);
        log.info("Audit Trail: Admin {} re-routing Application {} to Assignee {}",
                adminId, applicationId, payload.assigneeId());

        ApplicationResponse updated = applicationService.assign(applicationId, payload);

        return ResponseEntity.ok(Map.of(
                "status", "ASSIGNMENT_SUCCESS",
                "message", "Application successfully routed to designated underwriter.",
                "application", updated
        ));
    }
}