package com.pryme.Backend.config;

import com.pryme.Backend.config.dto.PolicyPatchRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 🧠 ZERO-TRUST POLICY ADMIN CONTROLLER
 * 
 * Secures the Matrix modifications. Only top-tier admins can reach this endpoint.
 * Idempotency and duplicate clicks are intercepted by the IdempotencyFilter 
 * upstream.
 */
@RestController
@RequestMapping("/api/v1/admin/policies")
public class PolicyAdminController {

    private static final Logger logger = LoggerFactory.getLogger(PolicyAdminController.class);

    private final com.pryme.Backend.config.service.PolicyAdminService policyAdminService;

    public PolicyAdminController(com.pryme.Backend.config.service.PolicyAdminService policyAdminService) {
        this.policyAdminService = policyAdminService;
    }

    @PatchMapping("/{entityId}/patch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POLICY_MANAGER')")
    public ResponseEntity<?> updatePolicyMatrix(
            @PathVariable String entityId,
            @Valid @RequestBody PolicyPatchRequest request) {

        logger.info("🛡️ MATRIX MUTATION REQUEST | EntityId: {} | FieldKey: {} | Mutated By: [EXTRACTED_BY_SECURITYContext]", 
                    entityId, request.fieldKey());
        logger.info("Audit Justification: {}", request.auditReason());

        // Ensure the path variable matches the payload to thwart payload-tampering attacks
        if (!entityId.equals(request.entityId())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Path entityId does not match payload entityId."
            ));
        }

        // Delegate to PolicyAdminService component which executes the atomic DB transaction.
        policyAdminService.applySurgicalPatch(request);

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Matrix updated successfully.",
            "fieldKey", request.fieldKey(),
            "newValue", request.newValue()
        ));
    }

    @GetMapping("/entities")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POLICY_MANAGER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<com.pryme.Backend.config.dto.PolicyEntityDto>> getPolicyEntities() {
        return ResponseEntity.ok(policyAdminService.getAllPolicyEntities());
    }

    @GetMapping("/value")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POLICY_MANAGER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> getPolicyValue(
            @RequestParam String entityId,
            @RequestParam String fieldKey) {
        
        Object value = policyAdminService.getPolicyValue(entityId, fieldKey);
        
        // React UI expects { "value": <the_value> }
        return ResponseEntity.ok(Map.of("value", value != null ? value : ""));
    }
}
