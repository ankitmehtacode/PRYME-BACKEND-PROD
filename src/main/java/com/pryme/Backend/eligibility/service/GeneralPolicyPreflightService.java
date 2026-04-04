package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import com.pryme.Backend.common.repository.PolicyFieldDefinitionRepository;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.PreflightRequest;
import com.pryme.Backend.eligibility.dto.PreflightResult;
import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeneralPolicyPreflightService {

    private final PolicyFieldDefinitionRepository policyFieldDefinitionRepository;
    private final EligibilityConditionRepository eligibilityConditionRepository;
    private final SpelExpressionCacheService spelExpressionCacheService;
    private final SimpleEvaluationContext simpleSandboxEvaluationContext;

    public PreflightResult evaluate(PreflightRequest request) {
        EligibilityRequest payload = request.request();
        List<String> violations = new ArrayList<>();

        if (payload.loanAmount() == null || payload.loanAmount().signum() <= 0) {
            violations.add("loanAmount must be greater than zero");
        }
        if (payload.propertyValue() == null || payload.propertyValue().signum() <= 0) {
            violations.add("propertyValue must be greater than zero");
        }
        if (payload.requestedTenureMonths() <= 0) {
            violations.add("requestedTenureMonths must be positive");
        }

        return new PreflightResult(violations.isEmpty(), violations);
    }

    public PolicyFieldDefinition validateAndSavePolicyFieldDefinition(PolicyFieldDefinition definition, String spelString) {
        preflightValidateSpel(spelString);
        return policyFieldDefinitionRepository.save(definition);
    }

    public EligibilityCondition validateAndSaveEligibilityCondition(EligibilityCondition condition, String spelString) {
        preflightValidateSpel(spelString);
        return eligibilityConditionRepository.save(condition);
    }

    private void preflightValidateSpel(String spelString) {
        Expression expression;
        try {
            expression = spelExpressionCacheService.getOrCompile(spelString);
        } catch (ParseException e) {
            throw new PolicyRuleValidationException("SpEL syntax error: " + e.getMessage());
        }

        Map<String, Object> mockPayload = buildMockPayload();
        try {
            expression.getValue(simpleSandboxEvaluationContext, mockPayload, Boolean.class);
        } catch (EvaluationException e) {
            throw new PolicyRuleValidationException("SpEL evaluation error: " + e.getMessage());
        }
    }

    private Map<String, Object> buildMockPayload() {
        Map<String, Object> mockPayload = new HashMap<>();
        for (PolicyFieldDefinition field : policyFieldDefinitionRepository.findAll()) {
            Object mockValue;
            switch (field.getFieldType()) {
                case NUMERIC_RANGE, PERCENTAGE, INTEGER -> mockValue = -1;
                case TEXT, ENUM_LIST -> mockValue = "";
                case BOOLEAN -> mockValue = false;
                default -> mockValue = null;
            }
            mockPayload.put(field.getFieldKey(), mockValue);
        }
        return mockPayload;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class PolicyRuleValidationException extends RuntimeException {
        public PolicyRuleValidationException(String message) {
            super(message);
        }
    }
}
