package com.pryme.Backend.config;

import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.common.entity.PolicyChangeAudit;
import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import com.pryme.Backend.common.repository.PolicyChangeAuditRepository;
import com.pryme.Backend.common.repository.PolicyFieldDefinitionRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 🧠 POLICY ADMIN CONTROLLER — THE RISK MATRIX GATEWAY
 *
 * This controller backs the frontend's AdminDashboard policy editor.
 * It serves field definitions (what can be edited) and handles policy value reads/writes
 * with a complete audit trail.
 *
 * Endpoints:
 *   GET  /api/v1/config/field-definitions?entityType=...  → List editable fields
 *   GET  /api/v1/policies/value?entityId=...&fieldKey=... → Read current field value
 *   PATCH /api/v1/policies                                → Write new value + audit
 *
 * Auth: All endpoints require authenticated session (anyRequest().authenticated())
 */
@RestController
@RequiredArgsConstructor
public class PolicyAdminController {

    private static final Logger log = LoggerFactory.getLogger(PolicyAdminController.class);

    private final PolicyFieldDefinitionRepository fieldDefinitionRepository;
    private final PolicyChangeAuditRepository auditRepository;

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧠 FIELD DEFINITIONS — What fields can the admin edit?
    // Used by the frontend DynamicPolicyInput factory to render the right input type
    // ═══════════════════════════════════════════════════════════════════════════

    @Operation(summary = "List active field definitions by entity type for policy editor")
    @GetMapping("/api/v1/config/field-definitions")
    public ResponseEntity<List<PolicyFieldDefinition>> getFieldDefinitions(
            @RequestParam String entityType) {

        PolicyFieldDefinition.PolicyEntityType type;
        try {
            type = PolicyFieldDefinition.PolicyEntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<PolicyFieldDefinition> fields = fieldDefinitionRepository
                .findByEntityTypeAndIsActive(type, true);

        return ResponseEntity.ok(fields);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧠 POLICY VALUE READ — Current value of a specific field for an entity
    // ═══════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Read current policy field value for a specific entity")
    @GetMapping("/api/v1/policies/value")
    public ResponseEntity<Map<String, Object>> getPolicyValue(
            @RequestParam String entityId,
            @RequestParam String fieldKey) {

        // Look up the field definition to know its type
        PolicyFieldDefinition fieldDef = fieldDefinitionRepository
                .findByFieldKeyAndEntityType(fieldKey, PolicyFieldDefinition.PolicyEntityType.LOAN_PRODUCT)
                .or(() -> fieldDefinitionRepository.findByFieldKeyAndEntityType(
                        fieldKey, PolicyFieldDefinition.PolicyEntityType.ELIGIBILITY_CONDITION))
                .or(() -> fieldDefinitionRepository.findByFieldKeyAndEntityType(
                        fieldKey, PolicyFieldDefinition.PolicyEntityType.GENERAL_BANK_POLICY))
                .orElseThrow(() -> new NotFoundException(
                        "Field definition not found for key: " + fieldKey));

        // Retrieve last applied value from audit trail
        List<PolicyChangeAudit> audits = auditRepository
                .findByEntityTypeAndEntityIdOrderByAppliedAtDesc(
                        fieldDef.getEntityType().name(),
                        Long.parseLong(entityId));

        // Find the most recent audit for this specific field
        String currentValue = audits.stream()
                .filter(a -> a.getFieldKey().equals(fieldKey))
                .findFirst()
                .map(PolicyChangeAudit::getNewValue)
                .orElse(null);

        return ResponseEntity.ok(Map.of(
                "entityId", entityId,
                "fieldKey", fieldKey,
                "value", currentValue != null ? currentValue : "",
                "fieldDefinition", fieldDef
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧠 POLICY MUTATION — Write new value with audit trail
    // This is the "blast radius" endpoint. Every change is logged.
    // ═══════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Update a policy field value with audit trail")
    @PatchMapping("/api/v1/policies")
    public ResponseEntity<Map<String, Object>> patchPolicy(
            @Valid @RequestBody PolicyPatchRequest request,
            HttpServletRequest httpRequest) {

        // Validate field exists
        PolicyFieldDefinition fieldDef = fieldDefinitionRepository
                .findByFieldKeyAndEntityType(request.fieldKey(),
                        PolicyFieldDefinition.PolicyEntityType.valueOf(request.entityType().toUpperCase()))
                .orElseThrow(() -> new NotFoundException(
                        "Field definition not found: " + request.fieldKey()));

        // Validate reason is provided for fields that require it
        if (fieldDef.isRequiresReason() &&
                (request.reason() == null || request.reason().trim().length() < 10)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Audit reason required",
                    "message", "This field requires a detailed change reason (min 10 chars)"
            ));
        }

        // Get previous value from audit trail
        String oldValue = auditRepository
                .findByEntityTypeAndEntityIdOrderByAppliedAtDesc(
                        request.entityType(), Long.parseLong(request.entityId()))
                .stream()
                .filter(a -> a.getFieldKey().equals(request.fieldKey()))
                .findFirst()
                .map(PolicyChangeAudit::getNewValue)
                .orElse(null);

        // Create audit record
        PolicyChangeAudit audit = PolicyChangeAudit.builder()
                .entityType(request.entityType())
                .entityId(Long.parseLong(request.entityId()))
                .fieldKey(request.fieldKey())
                .oldValue(oldValue)
                .newValue(request.newValue())
                .changedByUserId(0L) // TODO: Extract from session cookie
                .reason(request.reason())
                .ipAddress(httpRequest.getRemoteAddr())
                .effectiveFrom(LocalDateTime.now())
                .build();

        PolicyChangeAudit savedAudit = auditRepository.save(audit);

        log.warn("POLICY MUTATION: Field [{}] on entity [{}] changed from [{}] → [{}] by IP [{}]. Reason: {}",
                request.fieldKey(), request.entityId(),
                oldValue, request.newValue(),
                httpRequest.getRemoteAddr(), request.reason());

        return ResponseEntity.ok(Map.of(
                "status", "applied",
                "auditId", savedAudit.getId(),
                "fieldKey", request.fieldKey(),
                "oldValue", oldValue != null ? oldValue : "",
                "newValue", request.newValue(),
                "appliedAt", savedAudit.getAppliedAt() != null
                        ? savedAudit.getAppliedAt().toString() : LocalDateTime.now().toString()
        ));
    }

    /**
     * Request DTO for policy patch operations.
     */
    public record PolicyPatchRequest(
            @NotBlank String entityType,
            @NotBlank String entityId,
            @NotBlank String fieldKey,
            @NotNull String newValue,
            String reason,
            String idempotencyKey
    ) {}
}
