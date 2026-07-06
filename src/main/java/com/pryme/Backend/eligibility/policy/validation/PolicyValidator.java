package com.pryme.Backend.eligibility.policy.validation;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.normalization.PolicyNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PolicyValidator {

    private final PolicyNormalizer normalizer;
    private final StructuralPolicyValidator structuralValidator = new StructuralPolicyValidator();
    private final SemanticPolicyValidator semanticValidator = new SemanticPolicyValidator();
    private final ConflictPolicyValidator conflictValidator = new ConflictPolicyValidator();
    private final CrossReferencePolicyValidator crossReferenceValidator = new CrossReferencePolicyValidator();

    public PolicyValidationResult validate(PolicyBundle bundle) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        List<String> overlaps = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        List<String> orphans = new ArrayList<>();

        structuralValidator.validate(bundle, normalizer, errors, duplicates);
        semanticValidator.validate(bundle, normalizer, overlaps);
        conflictValidator.validate(bundle, conflicts);
        crossReferenceValidator.validate(bundle, normalizer, errors);

        boolean pass = errors.isEmpty() && duplicates.isEmpty() && overlaps.isEmpty() && conflicts.isEmpty();
        String severity = pass ? "INFO" : "ERROR";

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("eligibilityRulesCount", bundle.eligibilityRules() != null ? bundle.eligibilityRules().size() : 0);
        statistics.put("foirRulesCount", bundle.foirRules() != null ? bundle.foirRules().size() : 0);
        statistics.put("pfRulesCount", bundle.pfRules() != null ? bundle.pfRules().size() : 0);
        statistics.put("loginFeeRulesCount", bundle.loginFeeRules() != null ? bundle.loginFeeRules().size() : 0);
        statistics.put("lowLtvRulesCount", bundle.lowLtvRules() != null ? bundle.lowLtvRules().size() : 0);

        Map<String, Double> coverage = new HashMap<>();
        coverage.put("structuralPass", pass ? 100.0 : 0.0);

        return new PolicyValidationResult(
            pass,
            severity,
            errors,
            warnings,
            duplicates,
            overlaps,
            conflicts,
            orphans,
            statistics,
            coverage
        );
    }
}
