package com.pryme.Backend.eligibility.controller;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/eligibility")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EligibilityController {

    private final EligibilityEngineService eligibilityEngineService;

    @Operation(summary = "One-line description of this endpoint")
    @PostMapping("/evaluate")
    public ResponseEntity<List<EligibilityResult>> evaluate(@RequestBody @Valid EligibilityRequest request) {
        log.info("Eligibility evaluate request: loanType={}, empType={}, cibil={}, loanAmt={}",
                request.loanType(), request.employmentType(), request.cibilScore(), request.loanAmount());
        List<EligibilityResult> results = eligibilityEngineService.evaluate(request);
        // Always return 200 — the frontend inspects results to distinguish
        // eligible offers from rejections with reasons.
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping("/fields")
    public ResponseEntity<List<PolicyFieldDefinition>> getFields() {
        List<PolicyFieldDefinition> fields = eligibilityEngineService.getEligibilityConditionFields();
        if (fields.isEmpty()) {
            return ResponseEntity.status(501).body(null); // Return 501 if no fields
        }
        return ResponseEntity.ok(fields);
    }

    @Operation(summary = "One-line description of this endpoint")
    @PostMapping("/lnt-lap/best-match")
    public ResponseEntity<List<EligibilityResult>> bestMatch(@RequestBody @Valid EligibilityRequest request) {
        log.info("Eligibility best-match request: loanType={}, empType={}, cibil={}, loanAmt={}",
                request.loanType(), request.employmentType(), request.cibilScore(), request.loanAmount());
        List<EligibilityResult> results = eligibilityEngineService.evaluate(request);
        // Always return 200 — the frontend inspects results to distinguish
        // eligible offers from rejections with reasons.
        return ResponseEntity.ok(results);
    }
}
