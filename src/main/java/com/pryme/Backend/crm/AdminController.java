package com.pryme.Backend.crm;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.common.ForbiddenException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    // 1. MASTER DASHBOARD TELEMETRY (PAGINATED + RBAC-SCOPED)
    // ==========================================
    @Operation(summary = "List applications — EMPLOYEE sees only assigned, ADMIN sees all")
    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
            // 🧠 ELASTIC MEMORY PROTECTION: Defaults to 20 items per page to prevent RAM spikes.
            // React frontend can override via ?page=0&size=50
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        UUID adminId = extractAdminId(authentication);

        // 🧠 RBAC SCOPING: EMPLOYEE users see ONLY their assigned applications.
        // ADMIN/SUPER_ADMIN see the full Master Application Matrix.
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isEmployee && !isAdmin) {
            log.info("Audit Trail: Employee {} viewing assigned applications only. Page: {}, Size: {}",
                    adminId, pageable.getPageNumber(), pageable.getPageSize());
            return ResponseEntity.ok(applicationService.listAssignedApplications(adminId, pageable));
        }

        log.info("Audit Trail: Admin/Underwriter {} requested the Master Application Matrix. Page: {}, Size: {}",
                adminId, pageable.getPageNumber(), pageable.getPageSize());

        // 🧠 Passes the Pageable object deep into Hibernate
        return ResponseEntity.ok(applicationService.listApplications(pageable));
    }

    // ==========================================
    // 2. STATE TRANSITION ENGINE (APPROVE / REJECT)
    // ==========================================
    @Operation(summary = "Transition application status with audit trail")
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable String applicationId,
            @Valid @RequestBody UpdateStatusRequest payload,
            Authentication authentication
    ) {
        UUID adminId = extractAdminId(authentication);
        log.warn("Audit Trail: Admin {} initiating status transition to [{}] for Application {}",
                adminId, payload.status(), applicationId);

        // 🧠 AUDIT LEDGER PROPAGATION: Passes the adminId to the service boundary
        ApplicationResponse updated = applicationService.updateStatus(applicationId, payload, adminId);

        return ResponseEntity.ok(Map.of(
                "status", "TRANSITION_SUCCESS",
                "message", "Application state transition executed and secured.",
                "application", updated
        ));
    }

    // ==========================================
    // 3. ASSIGNMENT ROUTING ENGINE
    // ==========================================
    @Operation(summary = "Route application to designated underwriter")
    @PatchMapping("/{applicationId}/assign")
    public ResponseEntity<Map<String, Object>> assignApplication(
            @PathVariable String applicationId,
            @Valid @RequestBody AssignLeadRequest payload,
            Authentication authentication
    ) {
        UUID adminId = extractAdminId(authentication);
        log.info("Audit Trail: Admin {} re-routing Application {} to Assignee {}",
                adminId, applicationId, payload.assigneeId());

        // 🧠 AUDIT LEDGER PROPAGATION: Passes the adminId to the service boundary
        ApplicationResponse updated = applicationService.assign(applicationId, payload, adminId);

        return ResponseEntity.ok(Map.of(
                "status", "ASSIGNMENT_SUCCESS",
                "message", "Application successfully routed to designated underwriter.",
                "application", updated
        ));
    }

    // ==========================================
    // 4. LEAD PROFILE OVERRIDE ENGINE
    // ==========================================
    @Operation(summary = "Allows Super Admins and Admins to override lead profile data")
    @PatchMapping("/{applicationId}/profile")
    public ResponseEntity<Map<String, Object>> updateApplicationProfile(
            @PathVariable String applicationId,
            @Valid @RequestBody UpdateApplicationProfileRequest payload,
            Authentication authentication
    ) {
        UUID adminId = extractAdminId(authentication);
        log.warn("Audit Trail: Admin {} overriding profile data for Application {}", adminId, applicationId);

        ApplicationResponse updated = applicationService.updateProfile(applicationId, payload, adminId);

        return ResponseEntity.ok(Map.of(
                "status", "UPDATE_SUCCESS",
                "message", "Lead profile data successfully overridden.",
                "application", updated
        ));
    }
}