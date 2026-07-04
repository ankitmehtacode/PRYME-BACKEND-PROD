package com.pryme.Backend.eligibility.audit.certification;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PolicyOwnershipRegistry {

    public enum Owner {
        WORKBOOK,
        DATABASE
    }

    public enum PolicyDomain {
        ELIGIBILITY_RULES,
        FOIR,
        PROCESSING_FEE,
        LOGIN_FEE,
        HL_LTV,
        LAP_LTV,
        ROI_MATRIX
    }

    private static final Map<PolicyDomain, Owner> REGISTRY;

    static {
        Map<PolicyDomain, Owner> map = new LinkedHashMap<>();
        map.put(PolicyDomain.ELIGIBILITY_RULES, Owner.WORKBOOK);
        map.put(PolicyDomain.FOIR, Owner.WORKBOOK);
        map.put(PolicyDomain.PROCESSING_FEE, Owner.WORKBOOK);
        map.put(PolicyDomain.LOGIN_FEE, Owner.WORKBOOK);
        map.put(PolicyDomain.HL_LTV, Owner.WORKBOOK);
        map.put(PolicyDomain.LAP_LTV, Owner.WORKBOOK);
        map.put(PolicyDomain.ROI_MATRIX, Owner.DATABASE);
        REGISTRY = Collections.unmodifiableMap(map);
    }

    public Owner getOwner(PolicyDomain domain) {
        return REGISTRY.get(domain);
    }

    public Map<PolicyDomain, Owner> getRegistry() {
        return REGISTRY;
    }
}
