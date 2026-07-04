package com.pryme.Backend.eligibility.controller;

import com.pryme.Backend.eligibility.audit.certification.CertificationReportModels;
import com.pryme.Backend.eligibility.audit.certification.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/eligibility/certification")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class CertificationController {

    private final CertificationService certificationService;

    @Operation(summary = "Execute full 10-phase eligibility engine certification pipeline")
    @PostMapping("/certify")
    public ResponseEntity<CertificationReportModels.CertificationReport> certify() {
        log.info("Super Admin certification request received");
        CertificationReportModels.CertificationReport report = certificationService.runCertification();
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Get workbook structure & database cross-reference report")
    @GetMapping("/master-data")
    public ResponseEntity<CertificationReportModels.MasterDataAuditReport> getMasterDataReport() {
        CertificationReportModels.CertificationReport report = certificationService.runCertification();
        return ResponseEntity.ok(report.masterDataReport());
    }

    @Operation(summary = "Get rule execution and coverage telemetry")
    @GetMapping("/rule-coverage")
    public ResponseEntity<CertificationReportModels.RuleCoverageReport> getRuleCoverageReport() {
        CertificationReportModels.CertificationReport report = certificationService.runCertification();
        return ResponseEntity.ok(report.ruleCoverageReport());
    }

    @Operation(summary = "Get condition reachability status")
    @GetMapping("/reachability")
    public ResponseEntity<CertificationReportModels.ConditionReachabilityReport> getReachabilityReport() {
        CertificationReportModels.CertificationReport report = certificationService.runCertification();
        return ResponseEntity.ok(report.reachabilityReport());
    }
}
