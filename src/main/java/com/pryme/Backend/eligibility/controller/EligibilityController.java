package com.pryme.Backend.eligibility.controller;

import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import com.pryme.Backend.eligibility.EligibilityEngineService;
import com.pryme.Backend.eligibility.EligibilityRequest;
import com.pryme.Backend.eligibility.EligibilityResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/eligibility")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EligibilityController {

    private final EligibilityEngineService eligibilityEngineService;

    @PostMapping("/evaluate")
    public ResponseEntity<List<EligibilityResult>> evaluate(@RequestBody @Valid EligibilityRequest request) {
        List<EligibilityResult> results = eligibilityEngineService.evaluate(request);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        boolean allIneligible = results.stream().allMatch(result -> !result.isEligible());
        if (allIneligible) {
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/fields")
    public List<PolicyFieldDefinition> getFields() {
        // Assuming there's a method to fetch PolicyFieldDefinitions for ELIGIBILITY_CONDITION
        // This is a placeholder and should be replaced with actual implementation
        return eligibilityEngineService.getEligibilityConditionFields();
    }
}
