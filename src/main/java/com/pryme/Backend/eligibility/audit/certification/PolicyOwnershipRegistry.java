package com.pryme.Backend.eligibility.audit.certification;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PolicyOwnershipRegistry {

    public enum PolicySource {
        CLIENT_WORKBOOK,
        DATABASE,
        ENGINE_CONSTANT,
        DERIVED,
        USER_INPUT
    }

    public enum PolicyDomain {
        ELIGIBILITY_RULES,
        FOIR,
        PROCESSING_FEE,
        LOGIN_FEE,
        LOW_LTV_HL,
        LOW_LTV_LAP,
        ROI_MATRIX
    }

    public record PolicyDescriptor(
        PolicySource source,
        String workbookName,
        String sheetName,
        boolean crmEditable,
        boolean requiresRecertification
    ) {}

    private final Map<PolicyDomain, PolicyDescriptor> registry = new LinkedHashMap<>();
    private boolean frozen = false;

    public PolicyOwnershipRegistry() {
        registry.put(PolicyDomain.ELIGIBILITY_RULES, new PolicyDescriptor(PolicySource.CLIENT_WORKBOOK, "eligibility_workbook.xlsx", "eligibility_rules", false, true));
        registry.put(PolicyDomain.FOIR, new PolicyDescriptor(PolicySource.CLIENT_WORKBOOK, "FOIR_Sheet.xlsx", "FOIR", false, true));
        registry.put(PolicyDomain.PROCESSING_FEE, new PolicyDescriptor(PolicySource.CLIENT_WORKBOOK, "PF_data.xlsx", "PF", false, true));
        registry.put(PolicyDomain.LOGIN_FEE, new PolicyDescriptor(PolicySource.CLIENT_WORKBOOK, "Login_fees.xlsx", "Login", false, true));
        registry.put(PolicyDomain.LOW_LTV_HL, new PolicyDescriptor(PolicySource.CLIENT_WORKBOOK, "HL_LTV_Sheet.xlsx", "HL_LTV", false, true));
        registry.put(PolicyDomain.LOW_LTV_LAP, new PolicyDescriptor(PolicySource.CLIENT_WORKBOOK, "LAP_LTV_Sheet.xlsx", "LAP_LTV", false, true));
        registry.put(PolicyDomain.ROI_MATRIX, new PolicyDescriptor(PolicySource.DATABASE, "N/A", "ProductRoiMatrix", true, true));
        
        // Static registry can be frozen immediately in constructor
        freeze();
    }

    public void freeze() {
        this.frozen = true;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public PolicyDescriptor getDescriptor(PolicyDomain domain) {
        return registry.get(domain);
    }

    public PolicySource getSource(PolicyDomain domain) {
        PolicyDescriptor desc = registry.get(domain);
        return desc != null ? desc.source() : null;
    }

    public Map<PolicyDomain, PolicyDescriptor> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }
}
