package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.audit.DecisionTrace;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CertificationReportModels {

    public record FieldMismatch(
        String field,
        Object expected,
        Object actual,
        String message
    ) {}

    public record ConditionMismatch(
        Long conditionId,
        String productCode,
        String bankName,
        String employmentType,
        String surrogate,
        List<FieldMismatch> mismatches
    ) {}

    public record MasterDataAuditReport(
        int totalWorkbookRows,
        int totalActiveDbConditions,
        int activeMatchesCount,
        List<ConditionMismatch> mismatches,
        List<String> duplicateRows,
        List<String> invalidProductCodes,
        List<String> invalidLenderNames,
        boolean pass
    ) {}

    public record RuleCoverageItem(
        String ruleName,
        long executionCount,
        long passCount,
        long failCount,
        long skippedCount
    ) {}

    public record RuleCoverageReport(
        List<RuleCoverageItem> items,
        List<String> neverExecutedRules,
        boolean pass
    ) {}

    public record ReplayRowResult(
        int rowIndex,
        String bankName,
        String productCode,
        String employmentType,
        String surrogate,
        Boolean expectedEligible,
        Boolean actualEligible,
        String expectedProgram,
        String actualProgram,
        BigDecimal expectedAmount,
        BigDecimal actualAmount,
        BigDecimal expectedFoir,
        BigDecimal actualFoir,
        BigDecimal expectedRoi,
        BigDecimal actualRoi,
        BigDecimal expectedLtv,
        BigDecimal actualLtv,
        BigDecimal expectedProcessingFee,
        BigDecimal actualProcessingFee,
        BigDecimal expectedLoginFee,
        BigDecimal actualLoginFee,
        List<FieldMismatch> deviations,
        boolean pass
    ) {}

    public record SpreadsheetReplayReport(
        List<ReplayRowResult> rowResults,
        int totalRows,
        int passedRows,
        int failedRows,
        double passPercentage,
        boolean pass
    ) {}

    public record PipelineAuditItem(
        int rowIndex,
        String productCode,
        List<String> expectedPipeline,
        List<String> actualPipeline,
        boolean match
    ) {}

    public record PipelineAuditReport(
        List<PipelineAuditItem> items,
        int totalChecked,
        int matches,
        boolean pass
    ) {}

    public record FormulaDriftItem(
        String formulaName,
        String expression,
        Map<String, Object> inputs,
        BigDecimal expectedOutput,
        BigDecimal actualOutput,
        BigDecimal difference,
        BigDecimal tolerance,
        boolean pass
    ) {}

    public record FormulaDriftReport(
        List<FormulaDriftItem> items,
        int totalChecked,
        int failures,
        boolean pass
    ) {}

    public record SnapshotAuditReport(
        String engineVersion,
        String masterDataVersion,
        String workbookHash,
        String requestHash,
        String traceId,
        boolean determinismPass,
        boolean pass
    ) {}

    public record ConditionReachabilityItem(
        Long conditionId,
        String productCode,
        String bankName,
        String employmentType,
        String surrogate,
        boolean referenced,
        long executedCount,
        long selectedCount,
        boolean reachable
    ) {}

    public record ConditionReachabilityReport(
        List<ConditionReachabilityItem> items,
        int totalConditions,
        int reachableConditions,
        int unreachableConditions,
        boolean pass
    ) {}

    public record ClassifiedMismatch(
        String type, // row, formula, matrix, etc.
        String key,  // row index or condition ID
        String field,
        Object expected,
        Object actual,
        CertificationEnums.MismatchClassification classification,
        String remediation
    ) {}

    public record MismatchClassificationReport(
        List<ClassifiedMismatch> classifiedMismatches,
        Map<CertificationEnums.MismatchClassification, Integer> counts
    ) {}

    public record GateResult(
        CertificationEnums.CertificationGate gate,
        boolean pass,
        String message
    ) {}

    public record CertificationReport(
        String certificationId,
        Instant generatedAt,
        String engineVersion,
        String masterDataVersion,
        String workbookHash,
        String fingerprint,
        
        int conditionsAudited,
        double masterDataMatchPercentage,
        
        int rulesTotal,
        double ruleCoveragePercentage,
        
        int spreadsheetRowsCompared,
        double replayPassPercentage,
        
        int formulaDeviationsCount,
        int pipelineMismatchesCount,
        int reachabilityIssuesCount,
        
        boolean certified,
        List<GateResult> gates,
        
        MasterDataAuditReport masterDataReport,
        RuleCoverageReport ruleCoverageReport,
        SpreadsheetReplayReport replayReport,
        PipelineAuditReport pipelineReport,
        FormulaDriftReport formulaDriftReport,
        SnapshotAuditReport snapshotReport,
        ConditionReachabilityReport reachabilityReport,
        MismatchClassificationReport classificationReport
    ) {}
}
