package com.pryme.Backend.eligibility.policy.validation;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.normalization.PolicyNormalizer;
import java.util.*;

public class StructuralPolicyValidator {
    public void validate(PolicyBundle bundle, PolicyNormalizer normalizer, List<String> errors, List<String> duplicates) {
        Set<String> uniqueKeys = new HashSet<>();
        if (bundle.eligibilityRules() != null) {
            for (var row : bundle.eligibilityRules()) {
                String lender = row.lenderName();
                String code = row.productName();
                String key = String.format("%s:%s:%s", code, row.employmentType(), row.surrogate());
                if (uniqueKeys.contains(key)) {
                    duplicates.add("Duplicate row key in Eligibility rules: " + key);
                } else {
                    uniqueKeys.add(key);
                }

                if (lender == null || normalizer.normalizeLender(lender).isEmpty()) {
                    errors.add("Invalid lender in Eligibility rules: " + lender);
                }
                if (code == null || !code.contains("-")) {
                    errors.add("Invalid product code format: " + code);
                }
            }
        }
    }
}
