package com.pryme.Backend.eligibility.policy.validation;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import java.math.BigDecimal;
import java.util.List;

public class ConflictPolicyValidator {
    public void validate(PolicyBundle bundle, List<String> conflicts) {
        if (bundle.eligibilityRules() != null) {
            for (var row : bundle.eligibilityRules()) {
                if (row.minAge() != null && row.maxAge() != null && row.minAge() > row.maxAge()) {
                    conflicts.add(String.format("Conflicting age constraints for product %s: minAge %d > maxAge %d",
                            row.productName(), row.minAge(), row.maxAge()));
                }
                if (row.minIncome() != null && row.minIncome().compareTo(BigDecimal.ZERO) < 0) {
                    conflicts.add(String.format("Invalid negative minIncome for product %s: %s",
                            row.productName(), row.minIncome()));
                }
            }
        }
    }
}
