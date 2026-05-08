package com.pryme.Backend.crm;

import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/api/v1/admin/leads")
@RequiredArgsConstructor
public class AdminLeadController {

    private final LeadService leadService;

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping
    public ResponseEntity<Page<LeadResponse>> leads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(leadService.getLeads(pageable));
    }

    @Operation(summary = "Assign a lead to a team member")
    @PatchMapping("/{leadId}/assign")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<LeadResponse> assignLead(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID leadId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body
    ) {
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
}
