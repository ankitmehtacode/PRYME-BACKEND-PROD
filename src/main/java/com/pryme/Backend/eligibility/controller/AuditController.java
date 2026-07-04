package com.pryme.Backend.eligibility.controller;

import com.pryme.Backend.eligibility.audit.*;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/eligibility")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuditController {

    private final EligibilityEngineService eligibilityEngineService;
    private final MasterDataVersionService masterDataVersionService;

    @Operation(summary = "Evaluate eligibility with full decision trace")
    @PostMapping("/evaluate")
    public ResponseEntity<List<EligibilityResult>> evaluate(@RequestBody @Valid EligibilityRequest request) {
        log.info("Admin evaluate eligibility request received");
        List<EligibilityResult> results = eligibilityEngineService.evaluate(request);
        // Do NOT call toPublicResult() so decisionTrace is preserved and returned
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Generate an audit report for the request")
    @PostMapping("/audit")
    public ResponseEntity<AuditReport> audit(@RequestBody @Valid EligibilityRequest request) {
        long startTime = System.nanoTime();
        List<EligibilityResult> results = eligibilityEngineService.evaluate(request);
        long duration = (System.nanoTime() - startTime) / 1_000_000;

        List<DecisionTrace> traces = results.stream()
                .map(EligibilityResult::decisionTrace)
                .collect(Collectors.toList());

        String masterVersion = masterDataVersionService.computeVersion();
        String requestHash = computeRequestHash(request);

        AuditReport report = new AuditReport(
                traces,
                masterVersion,
                "1.0.0", // engineVersion
                duration,
                Instant.now(),
                requestHash
        );
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Compare engine result with expected Excel spreadsheet values")
    @PostMapping("/compare")
    public ResponseEntity<AuditComparisonResult> compare(@RequestBody @Valid AuditComparisonRequest comparisonRequest) {
        List<EligibilityResult> results = eligibilityEngineService.evaluate(comparisonRequest.profile());
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Match first product result
        EligibilityResult match = results.get(0);
        DecisionTrace trace = match.decisionTrace();
        
        AuditComparisonResult result = AuditComparisonResult.compare(trace, comparisonRequest.expected());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get the current version hash of the active master rules")
    @GetMapping("/master-version")
    public ResponseEntity<String> getMasterVersion() {
        return ResponseEntity.ok(masterDataVersionService.computeVersion());
    }

    private String computeRequestHash(EligibilityRequest request) {
        try {
            String input = String.format("%d:%s:%d:%d:%s:%s",
                    request.lenderId(),
                    request.loanType(),
                    request.cibilScore(),
                    request.applicantAge(),
                    request.loanAmount().toPlainString(),
                    request.idempotencyKey()
            );
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString();
        }
    }
}
