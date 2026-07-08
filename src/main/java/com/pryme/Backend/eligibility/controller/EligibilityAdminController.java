package com.pryme.Backend.eligibility.controller;

import com.pryme.Backend.eligibility.dto.PolicySnapshotDTO;
import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
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
    private final LoanProductRepository loanProductRepository;

    @Operation(summary = "List eligibility engine rules (defaults to active-only)")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Page<EligibilityCondition>> getAll(
            @RequestParam(value = "active", required = false) Boolean active,
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (active != null) {
            return ResponseEntity.ok(repository.findByActive(active, pageable));
        }
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
        if (rule.getBankName() == null || rule.getBankName().isBlank()) {
            loanProductRepository.findByProductCode(rule.getProductCode())
                    .ifPresent(product -> rule.setBankName(product.getLenderName()));
        }
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
                    if (incoming.getBankName() == null || incoming.getBankName().isBlank()) {
                        loanProductRepository.findByProductCode(incoming.getProductCode())
                                .ifPresent(product -> incoming.setBankName(product.getLenderName()));
                    }
                    EligibilityCondition saved = repository.save(incoming);
                    log.info("🧠 ENGINE RULE UPDATED [id={}] [productCode={}] by [{}]",
                            saved.getId(), saved.getProductCode(), auth.getName());
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a policy snapshot merging eligibility rules and loan product details")
    @GetMapping("/{id}/snapshot")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PolicySnapshotDTO> getSnapshot(@PathVariable Long id) {
        return repository.findById(id)
                .map(rule -> {
                    LoanProduct product = loanProductRepository.findByProductCode(rule.getProductCode())
                            .orElse(null);
                    
                    PolicySnapshotDTO snapshot = new PolicySnapshotDTO(
                            rule.getId(),
                            rule.getProductId(),
                            rule.getProductCode(),
                            rule.getEmploymentType(),
                            rule.getSurrogate(),
                            rule.getMinAge(),
                            rule.getMaxAge(),
                            rule.getMinIncome(),
                            rule.getIncomeType(),
                            rule.getWorkExpYears(),
                            rule.getBusinessAgeYears(),
                            rule.getCibilMin(),
                            rule.getLtvAllowed(),
                            rule.getBankName() != null && !rule.getBankName().isBlank() ? rule.getBankName() : (product != null ? product.getLenderName() : null),
                            rule.getLoanType(),
                            rule.getItrRequiredYears(),
                            rule.getDeviationFormulae(),
                            rule.getConditions(),
                            rule.getEmiNotObligated(),
                            rule.getPropertyType(),
                            rule.getNegativeProperty(),
                            rule.getNegativeEmployerType(),
                            rule.getNegativeSalaryMode(),
                            rule.getMarginByOccupation(),
                            rule.getProvidentFundMandatory(),
                            rule.getCityTier(),
                            rule.getProfileRestrictions(),
                            rule.getNotes(),
                            rule.isActive(),
                            
                            rule.getVintage(),
                            rule.getBankStatementRequirement(),
                            rule.getSalarySlipRequirement(),
                            rule.getGstReturnRequirement(),
                            rule.getLtvGrid(),
                            rule.getSelfEmployedProfessionals(),
                            rule.getFormulae(),
                            
                            product != null ? product.getProductName() : null,
                            product != null ? product.getLenderName() : null,
                            product != null ? product.getInterestType() : null,
                            product != null ? product.getRoi() : null,
                            product != null ? product.getPrepaymentCharges() : null,
                            product != null ? product.getForeclosureCharges() : null,
                            product != null ? product.getLoginFees() : null,
                            product != null ? product.getLegalTechnicalCharges() : null,
                            product != null ? product.getOtherExpense() : null,
                            product != null ? product.getInsuranceCharges() : null,
                            product != null ? product.getStampDuties() : null,
                            product != null ? product.getAdminFee() : null,
                            product != null ? product.getMinTenureMonths() : null,
                            product != null ? product.getMaxTenureMonths() : null,
                            product != null ? product.getMinLoanAmount() : null,
                            product != null ? product.getMaxLoanAmount() : null,
                            product != null ? product.getMinCibil() : null,
                            product != null ? product.getMaxCibil() : null,
                            product != null ? product.getKycRequirement() : null,
                            product != null ? product.getIncomeProof() : null,
                            product != null ? product.getBankStatementMonths() : null,
                            product != null ? product.getItrRequirementYears() : null,
                            product != null ? product.getSalarySlipMonths() : null,
                            product != null ? product.getGstRequiredMonths() : null,
                            product != null ? product.getResidenceProfile() : null,
                            product != null ? product.getAdditionalDocs() : null,
                            product != null ? product.getMaxEmiNmiRatio() : null,
                            product != null ? product.getLtv() : null,
                            product != null ? product.getObligationTreatment() : null,
                            product != null ? product.getDpdAllowed() : null,
                            product != null ? product.getWriteOffAllowed() : null,
                            product != null ? product.getSettlementAllowed() : null,
                            product != null ? product.getRiskCategory() : null,
                            product != null ? product.getOccupation() : null,
                            product != null ? product.getEmployerType() : null,
                            product != null ? product.getNatureOfBusiness() : null,
                            product != null ? product.getIndustry() : null,
                            product != null ? product.getPincodeRestrictions() : null,
                            product != null ? product.getCampaignName() : null,
                            product != null ? product.getOfferType() : null,
                            product != null ? product.getOfferDetails() : null
                    );
                    return ResponseEntity.ok(snapshot);
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
