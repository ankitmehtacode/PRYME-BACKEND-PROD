package com.pryme.Backend.eligibility.policy.engine;

import com.pryme.Backend.eligibility.policy.model.FoirPolicyRule;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import java.math.BigDecimal;

public class FoirResolver {

    private final CentralizedNormalizer normalizer;
    private static final BigDecimal DEFAULT_FOIR = new BigDecimal("0.65");

    public FoirResolver(CentralizedNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public BigDecimal resolve(
        PolicyBundle bundle,
        String lenderName,
        String surrogate,
        String employmentType,
        BigDecimal monthlyIncome,
        BigDecimal effectiveLtv,
        BigDecimal maxEmiNmiRatio
    ) {
        if (bundle != null && bundle.foirRules() != null) {
            String normLender = normalizer.normalizeLender(lenderName);
            String normEmp = normalizer.normalizeEmploymentType(employmentType);
            String normSurrogate = normalizer.normalizeSurrogate(surrogate);

            for (var row : bundle.foirRules()) {
                if (normalizer.normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                    && normalizer.normalizeSurrogate(row.surrogate()).equalsIgnoreCase(normSurrogate)
                    && normalizer.normalizeEmploymentType(row.employmentType()).equalsIgnoreCase(normEmp)) {

                    BigDecimal lower = row.lowerSalary() != null ? row.lowerSalary() : BigDecimal.ZERO;
                    BigDecimal upper = row.upperSalary() != null ? row.upperSalary() : new BigDecimal("999999999");

                    if (monthlyIncome != null && monthlyIncome.compareTo(lower) >= 0 && monthlyIncome.compareTo(upper) <= 0) {
                        if (row.foir() != null) {
                            return row.foir();
                        }
                    }
                }
            }
        }

        // ICICI Bank dynamic rule fallback
        String normLender = normalizer.normalizeLender(lenderName);
        String normEmp = normalizer.normalizeEmploymentType(employmentType);
        String normSurrogate = normalizer.normalizeSurrogate(surrogate);

        if ("ICICI Bank".equalsIgnoreCase(normLender)
                && "NIP".equalsIgnoreCase(normSurrogate)
                && (normEmp.contains("Self Employed") || normEmp.toLowerCase().contains("sep") || normEmp.toLowerCase().contains("senp"))) {
            BigDecimal ltv = effectiveLtv != null ? effectiveLtv : BigDecimal.ZERO;
            return new BigDecimal("1.40").subtract(ltv);
        }

        // Standard Product/System Fallback
        return maxEmiNmiRatio != null ? maxEmiNmiRatio : DEFAULT_FOIR;
    }
}

