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
            return ResponseEntity.status(501).body(null); // Return 501 if no results
        }
        boolean allIneligible = results.stream().allMatch(result -> !result.isEligible());
        if (allIneligible) {
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/fields")
    public ResponseEntity<List<PolicyFieldDefinition>> getFields() {
        List<PolicyFieldDefinition> fields = eligibilityEngineService.getEligibilityConditionFields();
        if (fields.isEmpty()) {
            return ResponseEntity.status(501).body(null); // Return 501 if no fields
        }
        return ResponseEntity.ok(fields);
    }

    @PostMapping("/lnt-lap/best-match")
    public ResponseEntity<List<EligibilityResult>> bestMatch(@RequestBody @Valid EligibilityRequest request) {
        List<EligibilityResult> results = eligibilityEngineService.evaluate(request);
        if (results.isEmpty()) {
            return ResponseEntity.status(501).body(null); // Return 501 if no results
        }
        boolean allIneligible = results.stream().allMatch(result -> !result.isEligible());
        if (allIneligible) {
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.ok(results);
    }
}
