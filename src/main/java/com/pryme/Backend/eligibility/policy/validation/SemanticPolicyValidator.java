package com.pryme.Backend.eligibility.policy.validation;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.normalization.PolicyNormalizer;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class SemanticPolicyValidator {
    public void validate(PolicyBundle bundle, PolicyNormalizer normalizer, List<String> overlaps) {
        // Validate overlapping salary slabs in FOIR
        if (bundle.foirRules() != null) {
            Map<String, List<com.pryme.Backend.eligibility.policy.model.FoirPolicyRule>> foirGroups = bundle.foirRules().stream()
                    .collect(Collectors.groupingBy(r -> String.format("%s:%s:%s",
                            normalizer.normalizeLender(r.lenderName()),
                            normalizer.normalizeSurrogate(r.surrogate()),
                            normalizer.normalizeEmploymentType(r.employmentType()))));

            for (var entry : foirGroups.entrySet()) {
                List<com.pryme.Backend.eligibility.policy.model.FoirPolicyRule> groupRows = new ArrayList<>(entry.getValue());
                groupRows.sort(Comparator.comparing(r -> r.lowerSalary() != null ? r.lowerSalary() : BigDecimal.ZERO));
                for (int i = 1; i < groupRows.size(); i++) {
                    var prev = groupRows.get(i - 1);
                    var curr = groupRows.get(i);
                    BigDecimal prevUpper = prev.upperSalary() != null ? prev.upperSalary() : new BigDecimal("999999999");
                    BigDecimal currLower = curr.lowerSalary() != null ? curr.lowerSalary() : BigDecimal.ZERO;
                    if (currLower.compareTo(prevUpper) <= 0) {
                        overlaps.add(String.format("Overlapping FOIR salary slab for %s: [%s - %s] overlaps with [%s - %s]",
                                entry.getKey(), prev.lowerSalary(), prev.upperSalary(), curr.lowerSalary(), curr.upperSalary()));
                    }
                }
            }
        }

        // Validate overlapping loan slabs in Processing Fees
        if (bundle.pfRules() != null) {
            Map<String, List<com.pryme.Backend.eligibility.policy.model.ProcessingFeeRule>> pfGroups = bundle.pfRules().stream()
                    .collect(Collectors.groupingBy(r -> String.format("%s:%s:%s",
                            normalizer.normalizeLender(r.lenderName()),
                            normalizer.normalizeLoanType(r.loanType()),
                            normalizer.normalizeEmploymentType(r.employmentType()))));

            for (var entry : pfGroups.entrySet()) {
                List<com.pryme.Backend.eligibility.policy.model.ProcessingFeeRule> groupRows = new ArrayList<>(entry.getValue());
                groupRows.sort(Comparator.comparing(r -> r.minLoanAmount() != null ? r.minLoanAmount() : BigDecimal.ZERO));
                for (int i = 1; i < groupRows.size(); i++) {
                    var prev = groupRows.get(i - 1);
                    var curr = groupRows.get(i);
                    BigDecimal prevMax = prev.maxLoanAmount() != null ? prev.maxLoanAmount() : new BigDecimal("999999999");
                    BigDecimal currMin = curr.minLoanAmount() != null ? curr.minLoanAmount() : BigDecimal.ZERO;
                    if (currMin.compareTo(prevMax) <= 0) {
                        overlaps.add(String.format("Overlapping PF loan slab for %s: [%s - %s] overlaps with [%s - %s]",
                                entry.getKey(), prev.minLoanAmount(), prev.maxLoanAmount(), curr.minLoanAmount(), curr.maxLoanAmount()));
                    }
                }
            }
        }
    }
}
