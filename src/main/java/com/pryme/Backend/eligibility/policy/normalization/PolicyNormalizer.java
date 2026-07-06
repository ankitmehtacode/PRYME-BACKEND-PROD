package com.pryme.Backend.eligibility.policy.normalization;

import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import org.springframework.stereotype.Component;

@Component
public class PolicyNormalizer {

    private final CentralizedNormalizer centralizedNormalizer;

    public PolicyNormalizer() {
        this.centralizedNormalizer = new CentralizedNormalizer();
    }

    @org.springframework.beans.factory.annotation.Autowired
    public PolicyNormalizer(CentralizedNormalizer centralizedNormalizer) {
        this.centralizedNormalizer = centralizedNormalizer;
    }

    public String normalizeLender(String name) {
        return centralizedNormalizer.normalizeLender(name);
    }

    public String normalizeEmploymentType(String empType) {
        return centralizedNormalizer.normalizeEmploymentType(empType);
    }

    public String normalizeSurrogate(String surrogate) {
        return centralizedNormalizer.normalizeSurrogate(surrogate);
    }

    public String normalizeLoanType(String loanType) {
        return centralizedNormalizer.normalizeLoanType(loanType);
    }
}
