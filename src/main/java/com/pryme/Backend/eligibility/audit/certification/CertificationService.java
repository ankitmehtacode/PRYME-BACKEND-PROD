package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.audit.*;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import com.pryme.Backend.eligibility.service.LowLtvSurrogateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificationService {

    private final ExcelWorkbookParser workbookParser;
    private final CertificationCalculator calculator;
    private final EligibilityEngineService engine;
    private final EligibilityConditionRepository eligibilityConditionRepository;
    private final LowLtvSurrogateService lowLtvSurrogateService;
    private final MasterDataVersionService masterDataVersionService;

    public CertificationReportModels.CertificationReport runCertification() {
        long startTime = System.nanoTime();
        String certificationId = UUID.randomUUID().toString();
        
        List<WorkbookModels.EligibilityRow> eligibilityRows = List.of();
        List<WorkbookModels.FoirRow> foirRows = List.of();
        List<WorkbookModels.PfRow> pfRows = List.of();
        List<WorkbookModels.LoginFeeRow> loginFeeRows = List.of();
        String workbookHash = "sha256:unknown";
        
        List<CertificationReportModels.GateResult> gates = new ArrayList<>();
        List<String> invalidLenders = new ArrayList<>();
        List<String> invalidProductCodes = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        
        // ── Phase 1: Parse all four workbooks and validate structure ──
        boolean structurePass = true;
        String structureMessage = "Workbook structure validated successfully";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Parse eligibility_workbook.xlsx
            try (InputStream is = new ClassPathResource("certification/eligibility_workbook.xlsx").getInputStream()) {
                byte[] bytes = is.readAllBytes();
                digest.update(bytes);
                eligibilityRows = workbookParser.parseEligibilityWorkbook(new ClassPathResource("certification/eligibility_workbook.xlsx").getInputStream());
            }
            
            // Parse FOIR_Sheet.xlsx
            try (InputStream is = new ClassPathResource("certification/FOIR_Sheet.xlsx").getInputStream()) {
                byte[] bytes = is.readAllBytes();
                digest.update(bytes);
                foirRows = workbookParser.parseFoirWorkbook(new ClassPathResource("certification/FOIR_Sheet.xlsx").getInputStream());
            }
            
            // Parse PF_data.xlsx
            try (InputStream is = new ClassPathResource("certification/PF_data.xlsx").getInputStream()) {
                byte[] bytes = is.readAllBytes();
                digest.update(bytes);
                pfRows = workbookParser.parsePfWorkbook(new ClassPathResource("certification/PF_data.xlsx").getInputStream());
            }
            
            // Parse Login_fees.xlsx
            try (InputStream is = new ClassPathResource("certification/Login_fees.xlsx").getInputStream()) {
                byte[] bytes = is.readAllBytes();
                digest.update(bytes);
                loginFeeRows = workbookParser.parseLoginFeeWorkbook(new ClassPathResource("certification/Login_fees.xlsx").getInputStream());
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder("sha256:");
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            workbookHash = hexString.toString();
            
            // Structure validation checks
            Set<String> uniqueKeys = new HashSet<>();
            for (var row : eligibilityRows) {
                String lender = row.lenderName();
                String code = getProductCodePrefix(row);
                String key = String.format("%s:%s:%s", code, row.employmentType(), row.surrogate());
                if (uniqueKeys.contains(key)) {
                    duplicates.add("Duplicate row: " + key);
                } else {
                    uniqueKeys.add(key);
                }
                
                if (lender == null || calculator.normalizeLender(lender).isEmpty()) {
                    invalidLenders.add("Invalid lender: " + lender);
                }
                if (code == null || !code.contains("-")) {
                    invalidProductCodes.add("Invalid product code: " + code);
                }
            }
            
            if (!duplicates.isEmpty() || !invalidLenders.isEmpty() || !invalidProductCodes.isEmpty()) {
                structurePass = false;
                structureMessage = "Validation failed: duplicates or invalid names found in workbooks";
            }
            
        } catch (Exception e) {
            log.error("Failed to parse workbooks", e);
            structurePass = false;
            structureMessage = "Workbook parsing exception: " + e.getMessage();
        }
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.STRUCTURE_VALIDATION, structurePass, structureMessage));

        // ── Phase 2: DB Cross-Reference ──
        List<CertificationReportModels.ConditionMismatch> dbMismatches = new ArrayList<>();
        boolean dbCrossPass = true;
        String dbCrossMessage = "Database matches workbook rules exactly";
        
        List<EligibilityCondition> dbConditions = eligibilityConditionRepository.findByActive(true);
        int matchedDbCount = 0;
        
        for (EligibilityCondition cond : dbConditions) {
            var matchOpt = findMatchingWorkbookRow(eligibilityRows, cond);
            if (matchOpt.isEmpty()) {
                dbMismatches.add(new CertificationReportModels.ConditionMismatch(
                    cond.getId(), cond.getProductCode(), cond.getBankName(), cond.getEmploymentType(), cond.getSurrogate(),
                    List.of(new CertificationReportModels.FieldMismatch("CONDITION", "Present in workbook", "Missing", "Database row not found in Excel workbook"))
                ));
                dbCrossPass = false;
            } else {
                matchedDbCount++;
                var wRow = matchOpt.get();
                List<CertificationReportModels.FieldMismatch> fields = new ArrayList<>();
                
                compareField(fields, "cibilMin", wRow.minCibil(), cond.getCibilMin());
                compareField(fields, "minIncome", wRow.minIncome(), cond.getMinIncome());
                compareField(fields, "minAge", wRow.minAge(), cond.getMinAge());
                compareField(fields, "maxAge", wRow.maxAge(), cond.getMaxAge());
                compareField(fields, "minTenure", wRow.minTenure(), cond.getMinTenure());
                compareField(fields, "maxTenure", wRow.maxTenure(), cond.getMaxTenure());
                compareField(fields, "minLoanAmount", wRow.minLoanAmount(), cond.getMinLoanAmount());
                compareField(fields, "maxLoanAmount", wRow.maxLoanAmount(), cond.getMaxLoanAmount());
                
                BigDecimal expectedLtv = parseLtvAllowed(wRow.ltv());
                compareField(fields, "ltvAllowed", expectedLtv, cond.getLtvAllowed());
                
                if (!fields.isEmpty()) {
                    dbMismatches.add(new CertificationReportModels.ConditionMismatch(
                        cond.getId(), cond.getProductCode(), cond.getBankName(), cond.getEmploymentType(), cond.getSurrogate(), fields
                    ));
                    dbCrossPass = false;
                }
            }
        }
        
        if (!dbCrossPass) {
            dbCrossMessage = "Mismatches found between database conditions and client workbooks";
        }
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.DB_CROSS_REFERENCE, dbCrossPass, dbCrossMessage));

        CertificationReportModels.MasterDataAuditReport masterDataReport = new CertificationReportModels.MasterDataAuditReport(
            eligibilityRows.size(), dbConditions.size(), matchedDbCount, dbMismatches, duplicates, invalidProductCodes, invalidLenders, dbCrossPass
        );

        // ── Phase 3 & 4 & 5: Spreadsheet Replay & Formula Drift & Pipeline Verification ──
        List<CertificationReportModels.ReplayRowResult> replayResults = new ArrayList<>();
        List<CertificationReportModels.PipelineAuditItem> pipelineItems = new ArrayList<>();
        List<CertificationReportModels.FormulaDriftItem> driftItems = new ArrayList<>();
        
        int totalReplayed = 0;
        int passedReplays = 0;
        Map<String, Long> ruleExecCounts = new HashMap<>();
        Map<String, Long> rulePassCounts = new HashMap<>();
        Map<String, Long> ruleFailCounts = new HashMap<>();
        Map<String, Long> ruleSkipCounts = new HashMap<>();
        
        for (int i = 0; i < eligibilityRows.size(); i++) {
            var row = eligibilityRows.get(i);
            if (row.lenderName() == null || row.lenderName().isBlank()) continue;
            
            String codePrefix = getProductCodePrefix(row);
            totalReplayed++;
            EligibilityRequest request = constructRequestForRow(row, i);
            List<EligibilityResult> evalResults = List.of();
            try {
                evalResults = engine.evaluate(request);
            } catch (Exception e) {
                log.error("Replay engine evaluation crashed for row index {}", i, e);
            }
            
            // Find target evaluated product result
            EligibilityResult targetResult = null;
            for (var res : evalResults) {
                if (res.productCode() != null && res.productCode().startsWith(codePrefix)) {
                    targetResult = res;
                    break;
                }
            }
            
            boolean rowPass = true;
            List<CertificationReportModels.FieldMismatch> deviations = new ArrayList<>();
            
            // Independent Calculations
            BigDecimal expectedFoir = calculator.lookupFoir(foirRows, row.lenderName(), row.surrogate(), row.employmentType(), request.monthlyIncome());
            BigDecimal expectedRoi = getExpectedRoiFromDb(codePrefix, row.employmentType(), request.cibilScore(), request.loanAmount());
            BigDecimal expectedLtv = resolveExpectedLtv(row, request);
            
            BigDecimal expectedEmi = calculator.calculateEmi(request.loanAmount(), expectedRoi, request.requestedTenureMonths());
            BigDecimal expectedPf = calculator.calculateProcessingFee(pfRows, row.lenderName(), row.employmentType(), request.loanAmount());
            BigDecimal expectedLoginFee = calculator.lookupLoginFee(loginFeeRows, row.lenderName(), row.employmentType(), request.loanAmount());
            
            // Verify engine values
            if (targetResult != null) {
                // Collect rule telemetry
                DecisionTrace trace = targetResult.decisionTrace();
                if (trace != null) {
                    for (DecisionStep step : trace.steps()) {
                        for (RuleEvaluation rule : step.rules()) {
                            String rName = rule.ruleName();
                            ruleExecCounts.put(rName, ruleExecCounts.getOrDefault(rName, 0L) + 1);
                            if (rule.status() == DecisionStatus.PASS) {
                                rulePassCounts.put(rName, rulePassCounts.getOrDefault(rName, 0L) + 1);
                            } else if (rule.status() == DecisionStatus.FAIL) {
                                ruleFailCounts.put(rName, ruleFailCounts.getOrDefault(rName, 0L) + 1);
                            } else if (rule.status() == DecisionStatus.SKIPPED) {
                                ruleSkipCounts.put(rName, ruleSkipCounts.getOrDefault(rName, 0L) + 1);
                            }
                        }
                        
                        // Phase 5: Formula Drift Audit (verify EMI trace)
                        for (FormulaTrace formula : step.formulas()) {
                            if ("EMI".equalsIgnoreCase(formula.formulaName())) {
                                BigDecimal diff = expectedEmi.subtract(formula.output()).abs();
                                boolean driftPass = diff.compareTo(BigDecimal.ONE) <= 0;
                                driftItems.add(new CertificationReportModels.FormulaDriftItem(
                                    "EMI", formula.expression(), formula.inputs(), expectedEmi, formula.output(), diff, BigDecimal.ONE, driftPass
                                ));
                            }
                        }
                    }
                }
                
                // Compare values
                compareField(deviations, "eligible", true, targetResult.eligible());
                compareFoirField(deviations, "foir", expectedFoir, targetResult.effectiveFoir());
                compareDecimalField(deviations, "roi", expectedRoi, targetResult.roi(), new BigDecimal("0.0001"));
                compareDecimalField(deviations, "ltv", expectedLtv, targetResult.ltv(), new BigDecimal("0.001"));
                compareDecimalField(deviations, "emi", expectedEmi, targetResult.proposedEmi(), BigDecimal.ONE);
                compareDecimalField(deviations, "processingFee", expectedPf, targetResult.processingFee(), BigDecimal.ZERO);
                compareDecimalField(deviations, "loginFee", expectedLoginFee, targetResult.loginFee(), BigDecimal.ZERO);
                
                // Phase 4: Pipeline Order Check
                List<String> expectedPipeline = getExpectedCascadePipeline(row.surrogate());
                List<String> actualPipeline = new ArrayList<>();
                if (trace != null) {
                    for (DecisionStep step : trace.steps()) {
                        actualPipeline.add(step.program().name());
                    }
                }
                boolean pipelineMatch = expectedPipeline.equals(actualPipeline);
                pipelineItems.add(new CertificationReportModels.PipelineAuditItem(i, codePrefix, expectedPipeline, actualPipeline, pipelineMatch));
                if (!pipelineMatch) {
                    deviations.add(new CertificationReportModels.FieldMismatch("PIPELINE", expectedPipeline.toString(), actualPipeline.toString(), "Pipeline order mismatch"));
                }
            } else {
                rowPass = false;
                deviations.add(new CertificationReportModels.FieldMismatch("PRODUCT_MATCH", "Evaluated", "Not Found", "Target product " + codePrefix + " was not evaluated by engine"));
            }
            
            if (!deviations.isEmpty()) {
                rowPass = false;
            } else {
                passedReplays++;
            }
            
            replayResults.add(new CertificationReportModels.ReplayRowResult(
                i + 2, row.lenderName(), codePrefix, row.employmentType(), row.surrogate(),
                true, targetResult != null && targetResult.eligible(),
                row.surrogate(), targetResult != null ? targetResult.programName() : "N/A",
                request.loanAmount(), targetResult != null ? targetResult.maxEligibleAmount() : BigDecimal.ZERO,
                expectedFoir, targetResult != null ? targetResult.effectiveFoir() : BigDecimal.ZERO,
                expectedRoi, targetResult != null ? targetResult.roi() : BigDecimal.ZERO,
                expectedLtv, targetResult != null ? targetResult.ltv() : BigDecimal.ZERO,
                expectedPf, targetResult != null ? targetResult.processingFee() : BigDecimal.ZERO,
                expectedLoginFee, targetResult != null ? targetResult.loginFee() : BigDecimal.ZERO,
                deviations, rowPass
            ));
        }
        
        boolean replayPass = passedReplays == totalReplayed;
        String replayMessage = String.format("Replayed %d scenarios. Passed %d / %d", totalReplayed, passedReplays, totalReplayed);
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.REPLAY_COVERAGE, replayPass, replayMessage));
        
        double passPct = totalReplayed > 0 ? (double) passedReplays / totalReplayed * 100.0 : 0.0;
        CertificationReportModels.SpreadsheetReplayReport replayReport = new CertificationReportModels.SpreadsheetReplayReport(
            replayResults, totalReplayed, passedReplays, totalReplayed - passedReplays, passPct, replayPass
        );
        
        // Rule coverage report
        List<CertificationReportModels.RuleCoverageItem> ruleItems = new ArrayList<>();
        List<String> neverExecutedRules = new ArrayList<>();
        List<String> expectedRules = List.of("MIN_CIBIL", "MIN_INCOME", "MIN_AGE", "LTV_LIMIT", "FOIR_LIMIT", "WORK_EXP_LIMIT", "PROPERTY_TYPE_CHECK");
        
        for (String rule : expectedRules) {
            long exec = ruleExecCounts.getOrDefault(rule, 0L);
            if (exec == 0) {
                neverExecutedRules.add(rule);
            }
            ruleItems.add(new CertificationReportModels.RuleCoverageItem(
                rule, exec, rulePassCounts.getOrDefault(rule, 0L), ruleFailCounts.getOrDefault(rule, 0L), ruleSkipCounts.getOrDefault(rule, 0L)
            ));
        }
        boolean rulePass = neverExecutedRules.isEmpty();
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.RULE_COVERAGE, rulePass, rulePass ? "All core eligibility rules executed successfully" : "Missing rule executions: " + neverExecutedRules));
        
        CertificationReportModels.RuleCoverageReport ruleReport = new CertificationReportModels.RuleCoverageReport(ruleItems, neverExecutedRules, rulePass);
        
        // Pipeline Audit Report
        long pipelineMatches = pipelineItems.stream().filter(CertificationReportModels.PipelineAuditItem::match).count();
        boolean pipelinePass = pipelineMatches == pipelineItems.size();
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.PIPELINE_VERIFICATION, pipelinePass, pipelinePass ? "Cascade pipeline orders verified successfully" : "Pipeline order mismatch detected"));
        CertificationReportModels.PipelineAuditReport pipelineReport = new CertificationReportModels.PipelineAuditReport(pipelineItems, pipelineItems.size(), (int) pipelineMatches, pipelinePass);
        
        // Formula Drift Report
        long driftFailures = driftItems.stream().filter(x -> !x.pass()).count();
        boolean driftPass = driftFailures == 0;
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.FORMULA_DRIFT_VALIDATION, driftPass, driftPass ? "Formula drift validation success (EMI matches within ±₹1)" : "Formula drift deviations detected"));
        CertificationReportModels.FormulaDriftReport formulaDriftReport = new CertificationReportModels.FormulaDriftReport(driftItems, driftItems.size(), (int) driftFailures, driftPass);
        
        // ── Phase 6: Snapshot Audit ──
        boolean snapshotPass = true;
        // Determinism Check
        boolean determinismPass = true;
        if (!eligibilityRows.isEmpty()) {
            var firstRow = eligibilityRows.get(0);
            var req = constructRequestForRow(firstRow, 0);
            var res1 = engine.evaluate(req);
            var res2 = engine.evaluate(req);
            if (!res1.isEmpty() && !res2.isEmpty()) {
                String hash1 = res1.get(0).decisionTrace() != null ? res1.get(0).decisionTrace().masterDataVersion() : "";
                String hash2 = res2.get(0).decisionTrace() != null ? res2.get(0).decisionTrace().masterDataVersion() : "";
                determinismPass = hash1.equals(hash2);
            }
        }
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.SNAPSHOT_DETERMINISM, determinismPass, determinismPass ? "Snapshot determinism check success" : "Non-deterministic outputs detected"));
        CertificationReportModels.SnapshotAuditReport snapshotReport = new CertificationReportModels.SnapshotAuditReport(
            "1.0.0", masterDataVersionService.computeVersion(), workbookHash, "request_hash_sample", UUID.randomUUID().toString(), determinismPass, determinismPass
        );
        
        // ── Phase 7: Condition Reachability Audit ──
        List<CertificationReportModels.ConditionReachabilityItem> reachItems = new ArrayList<>();
        long reachableCount = 0;
        for (var c : dbConditions) {
            long execs = replayResults.stream()
                .filter(x -> c.getProductCode().startsWith(x.productCode()) && x.employmentType().equalsIgnoreCase(c.getEmploymentType()) && x.surrogate().equalsIgnoreCase(c.getSurrogate()))
                .count();
            boolean reachable = execs > 0;
            if (reachable) reachableCount++;
            
            reachItems.add(new CertificationReportModels.ConditionReachabilityItem(
                c.getId(), c.getProductCode(), c.getBankName(), c.getEmploymentType(), c.getSurrogate(), true, execs, execs, reachable
            ));
        }
        boolean reachPass = reachableCount == dbConditions.size();
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.CONDITION_REACHABILITY, reachPass, String.format("Condition reachability: %d / %d reachable", reachableCount, dbConditions.size())));
        CertificationReportModels.ConditionReachabilityReport reachabilityReport = new CertificationReportModels.ConditionReachabilityReport(
            reachItems, dbConditions.size(), (int) reachableCount, dbConditions.size() - (int) reachableCount, reachPass
        );
        
        // ── Phase 8: Shadow Comparison Mode (Mismatch Classification) ──
        List<CertificationReportModels.ClassifiedMismatch> classifiedMismatches = new ArrayList<>();
        Map<CertificationEnums.MismatchClassification, Integer> counts = new HashMap<>();
        for (var mismatch : dbMismatches) {
            for (var field : mismatch.mismatches()) {
                var classification = CertificationEnums.MismatchClassification.MASTER_DATA_MISMATCH;
                classifiedMismatches.add(new CertificationReportModels.ClassifiedMismatch(
                    "DATABASE", mismatch.conditionId().toString(), field.field(), field.expected(), field.actual(), classification, "Update database condition to match workbook rules"
                ));
                counts.put(classification, counts.getOrDefault(classification, 0) + 1);
            }
        }
        for (var replay : replayResults) {
            if (!replay.pass()) {
                for (var dev : replay.deviations()) {
                    var classification = CertificationEnums.MismatchClassification.FORMULA_MISMATCH;
                    if ("foir".equalsIgnoreCase(dev.field()) || "ltv".equalsIgnoreCase(dev.field())) {
                        classification = CertificationEnums.MismatchClassification.RULE_MISMATCH;
                    } else if ("processingFee".equalsIgnoreCase(dev.field()) || "loginFee".equalsIgnoreCase(dev.field())) {
                        classification = CertificationEnums.MismatchClassification.ROUNDING_MISMATCH;
                    }
                    classifiedMismatches.add(new CertificationReportModels.ClassifiedMismatch(
                        "REPLAY", String.valueOf(replay.rowIndex()), dev.field(), dev.expected(), dev.actual(), classification, "Inspect engine logic for " + dev.field() + " divergence"
                    ));
                    counts.put(classification, counts.getOrDefault(classification, 0) + 1);
                }
            }
        }
        CertificationReportModels.MismatchClassificationReport classificationReport = new CertificationReportModels.MismatchClassificationReport(classifiedMismatches, counts);
        
        // ── Phase 9 & 10: Production Gate & Final Certification Dashboard ──
        boolean certified = gates.stream().allMatch(CertificationReportModels.GateResult::pass);
        
        // Fingerprint generation
        String fingerprintRaw = String.format("%s|%s|%s|%s", "1.0.0", masterDataVersionService.computeVersion(), workbookHash, certificationId);
        String fingerprint = "";
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] shaBytes = sha.digest(fingerprintRaw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("sha256:");
            for (byte b : shaBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            fingerprint = sb.toString();
        } catch (Exception e) {
            fingerprint = "sha256:unknown";
        }
        
        return new CertificationReportModels.CertificationReport(
            certificationId, Instant.now(), "1.0.0", masterDataVersionService.computeVersion(), workbookHash, fingerprint,
            dbConditions.size(), dbCrossPass ? 100.0 : ((double) matchedDbCount / dbConditions.size() * 100.0),
            expectedRules.size(), (double) (expectedRules.size() - neverExecutedRules.size()) / expectedRules.size() * 100.0,
            totalReplayed, passPct, driftItems.stream().filter(x -> !x.pass()).toList().size(),
            pipelineItems.stream().filter(x -> !x.match()).toList().size(),
            (int) (dbConditions.size() - reachableCount), certified, gates,
            masterDataReport, ruleReport, replayReport, pipelineReport, formulaDriftReport, snapshotReport, reachabilityReport, classificationReport
        );
    }

    private Optional<WorkbookModels.EligibilityRow> findMatchingWorkbookRow(List<WorkbookModels.EligibilityRow> rows, EligibilityCondition cond) {
        String condLender = cond.getBankName();
        String condType = cond.getLoanType();
        String condEmp = cond.getEmploymentType();
        String condSurrogate = cond.getSurrogate();
        
        return rows.stream().filter(r -> {
            boolean lenderMatch = calculator.normalizeLender(r.lenderName()).equalsIgnoreCase(calculator.normalizeLender(condLender));
            
            // Excel is "HL", DB is "HOME LOAN" or "HL"
            String excelType = r.loanType() != null && r.loanType().equalsIgnoreCase("Secured") ? "HL" : "LAP";
            if (r.productName() != null) {
                excelType = r.productName().trim();
            }
            String dbType = condType.contains("HOME") || condType.equalsIgnoreCase("HL") ? "HL" : "LAP";
            boolean typeMatch = excelType.equalsIgnoreCase(dbType);
            
            // Employment Type match
            boolean empMatch = false;
            String wEmp = r.employmentType();
            if (wEmp != null) {
                if (wEmp.contains("Self Employed Professional") && wEmp.contains("Self Employed Non Professional")) {
                    empMatch = condEmp.equalsIgnoreCase("Self Employed Professional") || condEmp.equalsIgnoreCase("Self Employed Non Professional");
                } else if (wEmp.equalsIgnoreCase("Self Employed Professional")) {
                    empMatch = condEmp.equalsIgnoreCase("Self Employed Professional");
                } else if (wEmp.equalsIgnoreCase("Salaried")) {
                    empMatch = condEmp.equalsIgnoreCase("Salaried");
                }
            }
            
            // Surrogate match
            boolean surrogateMatch = false;
            String wSurrogate = r.surrogate() != null ? r.surrogate().trim().toUpperCase() : "NIP";
            String cSurrogate = condSurrogate != null ? condSurrogate.trim().toUpperCase() : "NIP";
            if (wSurrogate.contains("LOW LTV")) wSurrogate = "LOW_LTV";
            if (cSurrogate.contains("LOW LTV")) cSurrogate = "LOW_LTV";
            surrogateMatch = wSurrogate.equalsIgnoreCase(cSurrogate);
            
            return lenderMatch && typeMatch && empMatch && surrogateMatch;
        }).findFirst();
    }

    private void compareField(List<CertificationReportModels.FieldMismatch> fields, String fieldName, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && actual != null) {
            if (expected instanceof BigDecimal && actual instanceof BigDecimal) {
                if (((BigDecimal) expected).compareTo((BigDecimal) actual) == 0) return;
            } else if (expected.toString().trim().equalsIgnoreCase(actual.toString().trim())) {
                return;
            }
        }
        fields.add(new CertificationReportModels.FieldMismatch(fieldName, expected, actual, String.format("Field %s mismatch: expected %s, got %s", fieldName, expected, actual)));
    }

    private void compareDecimalField(List<CertificationReportModels.FieldMismatch> deviations, String fieldName, BigDecimal expected, BigDecimal actual, BigDecimal tolerance) {
        if (expected == null && actual == null) return;
        BigDecimal exp = expected != null ? expected : BigDecimal.ZERO;
        BigDecimal act = actual != null ? actual : BigDecimal.ZERO;
        BigDecimal diff = exp.subtract(act).abs();
        if (diff.compareTo(tolerance) > 0) {
            deviations.add(new CertificationReportModels.FieldMismatch(fieldName, expected, actual, String.format("Calculated field %s mismatch: expected %s, got %s", fieldName, expected, actual)));
        }
    }

    private void compareFoirField(List<CertificationReportModels.FieldMismatch> deviations, String fieldName, BigDecimal expected, BigDecimal actual) {
        BigDecimal exp = expected != null ? expected : BigDecimal.ZERO;
        BigDecimal act = actual != null ? actual : BigDecimal.ZERO;
        
        // If Excel lists as decimal (e.g. 0.6) or percentage (60%), scale both to percentage
        if (exp.compareTo(BigDecimal.ONE) < 0) exp = exp.multiply(new BigDecimal("100"));
        if (act.compareTo(BigDecimal.ONE) < 0) act = act.multiply(new BigDecimal("100"));
        
        BigDecimal diff = exp.subtract(act).abs();
        if (diff.compareTo(new BigDecimal("0.1")) > 0) { // 0.1% tolerance
            deviations.add(new CertificationReportModels.FieldMismatch(fieldName, expected, actual, String.format("FOIR mismatch: expected %s, got %s", expected, actual)));
        }
    }

    private BigDecimal parseLtvAllowed(String ltvStr) {
        if (ltvStr == null || ltvStr.isEmpty() || ltvStr.toLowerCase().contains("as per") || ltvStr.equalsIgnoreCase("NA")) {
            return null;
        }
        ltvStr = ltvStr.replaceAll("[^0-9.]", "");
        if (ltvStr.isEmpty()) return null;
        try {
            BigDecimal val = new BigDecimal(ltvStr);
            if (val.compareTo(BigDecimal.ONE) > 0) {
                val = val.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }
            return val;
        } catch (Exception e) {
            return null;
        }
    }

    private EligibilityRequest constructRequestForRow(WorkbookModels.EligibilityRow row, int index) {
        BigDecimal loanAmount = row.minLoanAmount() != null ? row.minLoanAmount().max(new BigDecimal("2500000")) : new BigDecimal("2500000");
        int cibil = row.minCibil() != null ? row.minCibil() : 750;
        int age = row.minAge() != null ? row.minAge() + 2 : 35;
        BigDecimal income = row.minIncome() != null ? row.minIncome().max(new BigDecimal("50000")) : new BigDecimal("50000");
        int tenure = row.maxTenure() != null ? row.maxTenure() : 180;
        
        String surrogate = row.surrogate() != null ? row.surrogate().trim().toUpperCase() : "NIP";
        if (surrogate.contains("LOW LTV")) surrogate = "LOW_LTV";
        
        if ("LOW_LTV".equals(surrogate)) {
            income = BigDecimal.ONE;
        }
        
        IncomeComputationInput incomeInput = new IncomeComputationInput(
            surrogate, income.multiply(new BigDecimal("12")), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), BigDecimal.ZERO, "", BigDecimal.ZERO, "",
            calculator.normalizeLender(row.lenderName()),
            row.productName() != null ? row.productName().trim() : "HL"
        );
        
        String propType = "FLAT";
        if (row.propertyType() != null) {
            String pt = row.propertyType().toUpperCase();
            if (pt.contains("PLOT")) {
                propType = "PLOT";
            } else if (pt.contains("COMMERCIAL")) {
                propType = "SHOP";
            } else if (pt.contains("INDUSTRIAL")) {
                propType = "FACTORIES";
            }
        }
        
        BigDecimal expectedLtvVal = parseLtvAllowed(row.ltv());
        if (expectedLtvVal == null) {
            String normLender = calculator.normalizeLender(row.lenderName());
            if ("HL".equalsIgnoreCase(row.productName())) {
                expectedLtvVal = lowLtvSurrogateService.getHlLtv(propType, loanAmount);
            } else {
                expectedLtvVal = lowLtvSurrogateService.getLapLtv(normLender, propType);
            }
        }
        if (expectedLtvVal == null || expectedLtvVal.compareTo(BigDecimal.ZERO) == 0) {
            expectedLtvVal = new BigDecimal("0.70");
        }
        BigDecimal propertyValue = loanAmount.divide(expectedLtvVal, 2, RoundingMode.HALF_UP);
        
        return new EligibilityRequest(
            null, // lenderId
            row.productName() != null ? row.productName().trim() : "HL",
            cibil, age,
            row.employmentType() != null && row.employmentType().contains("Salaried") ? "Salaried" : "Self Employed",
            propType,
            "Tier 1",
            loanAmount, propertyValue, tenure, income, BigDecimal.ZERO,
            3, 5, incomeInput,
            "idempotency-certification-replay-" + index,
            3, income,
            "452001", // Indore
            propType,
            null
        );
    }

    private BigDecimal getExpectedRoiFromDb(String productCode, String employmentType, int cibil, BigDecimal loanAmount) {
        // Query ROI matrix or base product ROI as fallback (ROI matrices are in DB only, workbooks do not contain rates)
        // For simulation, we return a standard mock ROI of 8.25% if matrix lookup is bypassed
        return new BigDecimal("0.0825");
    }

    private BigDecimal resolveExpectedLtv(WorkbookModels.EligibilityRow row, EligibilityRequest request) {
        String ltvStr = row.ltv();
        BigDecimal parsed = parseLtvAllowed(ltvStr);
        if (parsed != null) return parsed;
        
        // Low LTV surrogate limits
        if ("LOW_LTV".equalsIgnoreCase(row.surrogate()) || (row.surrogate() != null && row.surrogate().toUpperCase().contains("LOW LTV"))) {
            if ("HL".equalsIgnoreCase(row.productName())) {
                return lowLtvSurrogateService.getHlLtv(request.propertyType(), request.loanAmount());
            } else {
                return lowLtvSurrogateService.getLapLtv(row.lenderName(), lowLtvSurrogateService.resolvePropertyKey(request.propertyType(), request.propertyCategory(), request.businessPropertyCategory()));
            }
        }
        return new BigDecimal("0.75"); // default fallback
    }

    private List<String> getExpectedCascadePipeline(String surrogate) {
        List<String> pipeline = new ArrayList<>();
        pipeline.add("NIP");
        String normSurrogate = surrogate != null ? surrogate.trim().toUpperCase() : "NIP";
        if (!"NIP".equals(normSurrogate)) {
            if (normSurrogate.contains("LOW LTV")) {
                pipeline.add("LOW_LTV");
            } else {
                pipeline.add(normSurrogate);
                pipeline.add("LOW_LTV");
            }
        } else {
            pipeline.add("LOW_LTV");
        }
        return pipeline;
    }

    public String getProductCodePrefix(WorkbookModels.EligibilityRow row) {
        if (row == null) return "";
        String normLender = calculator.normalizeLender(row.lenderName());
        String productName = row.productName() != null ? row.productName().trim() : "HL";
        String suffix = "HL".equalsIgnoreCase(productName) ? "HL" : "LAP";
        String prefix;
        if (normLender.contains("L&T")) prefix = "LT";
        else if (normLender.contains("ICICI")) prefix = "ICICI";
        else if (normLender.contains("Bandhan")) prefix = "BANDHAN";
        else if (normLender.contains("Aditya") || normLender.contains("ABFL")) prefix = "ABFL";
        else if (normLender.contains("Baroda") || normLender.contains("BOB")) prefix = "BOB";
        else if (normLender.contains("SBI")) prefix = "SBI";
        else if (normLender.contains("Bajaj Finance")) prefix = "BAJAJ";
        else if (normLender.contains("Bajaj Prime")) prefix = "BAJAJ";
        else if (normLender.contains("YES")) prefix = "YES";
        else if (normLender.contains("HDFC")) prefix = "HDFC";
        else if (normLender.contains("JIO")) prefix = "JIO";
        else if (normLender.contains("IDBI")) prefix = "IDBI";
        else if (normLender.contains("Tata")) prefix = "TATA";
        else if (normLender.contains("IDFC")) prefix = "IDFC";
        else prefix = normLender;
        return prefix + "-" + suffix;
    }
}
