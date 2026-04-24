package com.pryme.Backend.eligibility.controller;

import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 🧠 ENGINE RULES ADMIN CONTROLLER
 *
 * Full CRUD for EligibilityCondition rows — the backbone of the Matrix engine.
 * Every change here directly affects which loan products a user sees in "See My Offers".
 *
 * Security: Only SUPER_ADMIN and ADMIN can invoke.
 * Audit: Every mutation is logged with the caller's identity.
 */
@RestController
@RequestMapping("/api/v1/admin/eligibility-rules")
@RequiredArgsConstructor
@Slf4j
public class EligibilityAdminController {

    private final EligibilityConditionRepository repository;

    @Operation(summary = "List all eligibility engine rules")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Page<EligibilityCondition>> getAll(
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @Operation(summary = "Get a single rule by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<EligibilityCondition> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new eligibility rule")
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<EligibilityCondition> create(
            @RequestBody EligibilityCondition rule,
            Authentication auth
    ) {
        rule.setId(null); // Force new entity
        EligibilityCondition saved = repository.save(rule);
        log.info("🧠 ENGINE RULE CREATED [id={}] [productCode={}] by [{}]",
                saved.getId(), saved.getProductCode(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Update an existing eligibility rule")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody EligibilityCondition incoming,
            Authentication auth
    ) {
        return repository.findById(id)
                .map(existing -> {
                    // Preserve immutable audit fields
                    incoming.setId(existing.getId());
                    incoming.setCreatedAt(existing.getCreatedAt());
                    EligibilityCondition saved = repository.save(incoming);
                    log.info("🧠 ENGINE RULE UPDATED [id={}] [productCode={}] by [{}]",
                            saved.getId(), saved.getProductCode(), auth.getName());
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an eligibility rule")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        log.info("🧠 ENGINE RULE DELETED [id={}] by [{}]", id, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Rule deleted.", "id", id));
    }
}
