package com.pryme.Backend.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 🧠 ZERO-TRUST POLICY MUTATION DTO
 * 
 * Strict contract for modifying the Matrix. Extracted via the IdempotencyFilter 
 * to ensure that identical mutations are dropped and audit reasoning is enforced 
 * BEFORE reaching the service layer.
 */
public record PolicyPatchRequest(
    
    @NotBlank(message = "Entity Type is required (e.g., LOAN_PRODUCT, SURROGATE_PROGRAM)")
    String entityType,
    
    @NotBlank(message = "Entity ID is required")
    String entityId,
    
    @NotBlank(message = "Field Key is required")
    String fieldKey,
    
    Object oldValue,
    
    @NotNull(message = "New Value cannot be null (use empty string if clearing)")
    Object newValue,
    
    @NotBlank(message = "Audit Justification is strictly required for Matrix changes")
    @Size(min = 10, message = "Audit reason must be at least 10 characters long to provide sufficient context")
    String auditReason
) {}
