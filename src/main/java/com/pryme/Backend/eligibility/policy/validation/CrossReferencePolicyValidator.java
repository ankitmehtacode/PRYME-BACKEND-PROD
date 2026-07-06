package com.pryme.Backend.eligibility.policy.validation;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.normalization.PolicyNormalizer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CrossReferencePolicyValidator {
    public void validate(PolicyBundle bundle, PolicyNormalizer normalizer, List<String> errors) {
        if (bundle.eligibilityRules() == null) return;

        Set<String> foirLenders = bundle.foirRules().stream()
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());
        Set<String> pfLenders = bundle.pfRules().stream()
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());
        Set<String> loginLenders = bundle.loginFeeRules().stream()
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());
        Set<String> lapLenders = bundle.lowLtvRules().stream()
                .filter(r -> "LAP".equalsIgnoreCase(r.loanType()))
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());

        for (var row : bundle.eligibilityRules()) {
            String lender = row.lenderName();
            if (lender == null || lender.isBlank()) continue;
            String normLender = normalizer.normalizeLender(lender);

            if (!foirLenders.contains(normLender)) {
                errors.add(String.format("Lender '%s' from Eligibility rules not found in FOIR matrix", lender));
            }
            if (!pfLenders.contains(normLender)) {
                errors.add(String.format("Lender '%s' from Eligibility rules not found in PF matrix", lender));
            }
            if (!loginLenders.contains(normLender)) {
                errors.add(String.format("Lender '%s' from Eligibility rules not found in Login Fees matrix", lender));
            }
            if ("LAP".equalsIgnoreCase(row.loanType()) && !lapLenders.contains(normLender)) {
                errors.add(String.format("LAP Lender '%s' from Eligibility rules not found in LAP LTV rules", lender));
            }
        }
    }
}
