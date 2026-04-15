package com.pryme.Backend.config.service;

import com.pryme.Backend.common.entity.PolicyChangeAudit;
import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import com.pryme.Backend.common.repository.PolicyChangeAuditRepository;
import com.pryme.Backend.common.repository.PolicyFieldDefinitionRepository;
import com.pryme.Backend.config.dto.PolicyPatchRequest;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PolicyAdminService {

    private final LoanProductRepository loanProductRepository;
    private final PolicyFieldDefinitionRepository fieldDefinitionRepository;
    private final PolicyChangeAuditRepository auditRepository;

    public PolicyAdminService(LoanProductRepository loanProductRepository,
                              PolicyFieldDefinitionRepository fieldDefinitionRepository,
                              PolicyChangeAuditRepository auditRepository) {
        this.loanProductRepository = loanProductRepository;
        this.fieldDefinitionRepository = fieldDefinitionRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void applySurgicalPatch(PolicyPatchRequest request) {

        PolicyFieldDefinition.PolicyEntityType enumType;
        try {
            enumType = PolicyFieldDefinition.PolicyEntityType.valueOf(request.entityType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid entity type: " + request.entityType());
        }

        // 1. Validate Field Definition
        PolicyFieldDefinition fieldDef = fieldDefinitionRepository.findByFieldKeyAndEntityType(request.fieldKey(), enumType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown field key for this entity: " + request.fieldKey()));

        // 2. Route by Entity Type and Execute Safe Strategy
        Long id = Long.parseLong(request.entityId());
        
        switch (enumType) {
            case LOAN_PRODUCT:
                LoanProduct product = loanProductRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                applyToLoanProduct(product, fieldDef, String.valueOf(request.newValue()));
                loanProductRepository.save(product);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported matrix entity type: " + request.entityType());
        }

        // 3. The Immutable Audit Trail (Failproof Guardrail)
        // Wrapped in the same @Transactional block. If this fails, the DB row reverts.
        Long adminId = extractAdminId();
        
        PolicyChangeAudit audit = PolicyChangeAudit.builder()
                .entityType(request.entityType())
                .entityId(id)
                .fieldKey(request.fieldKey())
                .oldValue(request.oldValue() != null ? String.valueOf(request.oldValue()) : null)
                .newValue(String.valueOf(request.newValue()))
                .reason(request.auditReason())
                .changedByUserId(adminId)
                .effectiveFrom(LocalDateTime.now())
                .ipAddress("UNKNOWN") // Sourced via security config/filter in a full implementation
                .build();
                
        auditRepository.save(audit);
    }

    /**
     * The Reflection/Mapper Safely: Strict switch mapped to setters.
     * Prevents blind reflection instantiation attacks.
     */
    private void applyToLoanProduct(LoanProduct product, PolicyFieldDefinition def, String rawNewValue) {
        Object typedValue = castSafely(rawNewValue, def.getFieldType());

        switch (def.getFieldKey()) {
            case "min_cibil":
                product.setMinCibil((Integer) typedValue);
                break;
            case "max_cibil":
                product.setMaxCibil((Integer) typedValue);
                break;
            case "roi":
                product.setRoi((BigDecimal) typedValue);
                break;
            case "processing_fee":
                product.setProcessingFee((BigDecimal) typedValue);
                break;
            case "max_emi_nmi_ratio":
                product.setMaxEmiNmiRatio((BigDecimal) typedValue);
                break;
            case "min_loan_amount":
                product.setMinLoanAmount((BigDecimal) typedValue);
                break;
            case "max_loan_amount":
                product.setMaxLoanAmount((BigDecimal) typedValue);
                break;
            case "min_tenure_months":
                product.setMinTenureMonths((Integer) typedValue);
                break;
            case "max_tenure_months":
                product.setMaxTenureMonths((Integer) typedValue);
                break;
            case "is_active":
                product.setActive((Boolean) typedValue);
                break;
            default:
                throw new IllegalArgumentException("Secure Mapper block: Field " + def.getFieldKey() + " is not mapped for LOAN_PRODUCT updates.");
        }
    }

    /**
     * Type Casting: Casts the incoming String to the DB-supported primitive.
     */
    private Object castSafely(String value, PolicyFieldDefinition.FieldType fieldType) {
        try {
            switch (fieldType) {
                case INTEGER:
                    return Integer.valueOf(value);
                case BOOLEAN:
                    return Boolean.valueOf(value);
                case NUMERIC_RANGE:
                case PERCENTAGE:
                    return new BigDecimal(value);
                case ENUM_LIST:
                case TEXT:
                default:
                    return value;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Type mismatch: Cannot safely cast '" + value + "' to " + fieldType);
        }
    }

    private Long extractAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) return 0L;
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return 0L; 
        }
    }
}
