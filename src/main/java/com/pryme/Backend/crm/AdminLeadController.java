package com.pryme.Backend.crm;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/leads")
@RequiredArgsConstructor
// 🧠 CLASS-LEVEL RBAC GATE: Mathematically prevents any standard USER from
// accessing the admin lead pipeline. EMPLOYEE can view assigned leads;
// ADMIN/SUPER_ADMIN can view all + assign.
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
public class AdminLeadController {

    private final LeadService leadService;
    private final UserRepository userRepository;

    // ==========================================
    // 🧠 PRINCIPAL EXTRACTOR (Shared across endpoints)
    // ==========================================
    private UUID extractCallerId(Authentication auth) {
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

    @Operation(summary = "List leads with RBAC scoping — EMPLOYEE sees only assigned leads")
    @GetMapping
    public ResponseEntity<Page<LeadResponse>> leads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 🧠 RBAC SCOPING: Delegates to LeadService which inspects the caller's role
        // and returns only assigned leads for EMPLOYEE, or all leads for ADMIN+.
        return ResponseEntity.ok(leadService.getLeads(pageable));
    }

    @Operation(summary = "Assign a lead to a team member (ADMIN/SUPER_ADMIN only)")
    @PatchMapping("/{leadId}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<LeadResponse> assignLead(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID leadId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body,
            Authentication authentication
    ) {
        // 🧠 AUDIT: Log who performed the assignment
        UUID adminId = extractCallerId(authentication);

        String assigneeIdStr = body.get("assigneeId");
        java.util.UUID assigneeId = null;
        if (assigneeIdStr != null && !assigneeIdStr.isBlank()) {
            try {
                assigneeId = java.util.UUID.fromString(assigneeIdStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(leadService.assignLead(leadId, assigneeId));
    }

    @Operation(summary = "Update a lead's status (ADMIN/SUPER_ADMIN/EMPLOYEE)")
    @PatchMapping("/{leadId}/status")
    public ResponseEntity<LeadResponse> updateLeadStatus(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID leadId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body,
            Authentication authentication
    ) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        LeadStatus status;
        try {
            status = LeadStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(leadService.updateLeadStatus(leadId, status));
    }
}
