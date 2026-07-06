package com.pryme.Backend.eligibility.policy.engine;

import com.pryme.Backend.eligibility.policy.model.EligibilityPolicyRule;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import java.util.List;
import java.util.stream.Collectors;

public class EligibilityResolver {
    public List<EligibilityPolicyRule> resolve(PolicyBundle bundle, String lenderName, String loanType) {
        return bundle.eligibilityRules().stream()
            .filter(r -> (lenderName == null || isLenderMatch(lenderName, r.lenderName()))
                      && (loanType == null || isLoanTypeMatch(loanType, r.loanType())))
            .collect(Collectors.toList());
    }

    private boolean isLenderMatch(String inputName, String targetName) {
        if (inputName == null || targetName == null) return false;
        String lowerInput = inputName.toLowerCase().replaceAll("[^a-z0-9]", "");
        String lowerTarget = targetName.toLowerCase().replaceAll("[^a-z0-9]", "");
        return lowerInput.contains(lowerTarget) || lowerTarget.contains(lowerInput);
    }

    private boolean isLoanTypeMatch(String productLoanType, String rowProductName) {
        if (productLoanType == null || rowProductName == null) return false;
        String cleanProd = productLoanType.trim().toUpperCase();
        String cleanRow = rowProductName.trim().toUpperCase();
        return cleanProd.equalsIgnoreCase(cleanRow) || cleanProd.contains(cleanRow) || cleanRow.contains(cleanProd);
    }
}
