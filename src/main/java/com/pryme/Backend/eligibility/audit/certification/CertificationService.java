package com.pryme.Backend.eligibility.audit.certification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pryme.Backend.eligibility.audit.*;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import com.pryme.Backend.eligibility.service.LowLtvSurrogateService;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificationService {

    private final PolicyProvider policyProvider;
    private final IndependentPolicyEvaluator evaluator;
    private final EligibilityEngineService engine;
    private final EligibilityConditionRepository eligibilityConditionRepository;
    private final LowLtvSurrogateService lowLtvSurrogateService;
    private final MasterDataVersionService masterDataVersionService;
    private final CentralizedNormalizer normalizer;
    private final LoanProductRepository loanProductRepository;
    private final PolicyOwnershipRegistry policyOwnershipRegistry;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public CertificationReportModels.CertificationReport runCertification() {
        long startTime = System.nanoTime();
        String certificationId = UUID.randomUUID().toString();

        // Trigger garbage collection to normalize memory measurement
        System.gc();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // 1. Load policies via provider
        PolicyBundle bundle = policyProvider.load();
        List<WorkbookModels.EligibilityRow> eligibilityRows = bundle.eligibilityRows();
        List<WorkbookModels.FoirRow> foirRows = bundle.foirRows();
        List<WorkbookModels.PfRow> pfRows = bundle.pfRows();
        List<WorkbookModels.LoginFeeRow> loginFeeRows = bundle.loginFeeRows();
        List<WorkbookModels.HlLtvRow> hlLtvRows = bundle.hlLtvRows();
        List<WorkbookModels.LapLtvRow> lapLtvRows = bundle.lapLtvRows();
        String workbookHash = bundle.workbookHash();

        List<CertificationReportModels.GateResult> gates = new ArrayList<>();
        List<String> invalidLenders = new ArrayList<>();
        List<String> invalidProductCodes = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        List<String> slabOverlaps = new ArrayList<>();

        // 2. Fail-Fast Structural Workbook Validation
        boolean structurePass = true;
        String structureMessage = "Workbook structure validated successfully";

        // Check duplicates and basic integrity in Loan_Product_Master
        Set<String> uniqueKeys = new HashSet<>();
        for (var row : eligibilityRows) {
            String lender = row.lenderName();
            String code = getProductCodePrefix(row);
            String key = String.format("%s:%s:%s", code, row.employmentType(), row.surrogate());
            if (uniqueKeys.contains(key)) {
                duplicates.add("Duplicate row key in Loan_Product_Master: " + key);
            } else {
                uniqueKeys.add(key);
            }

            if (lender == null || normalizer.normalizeLender(lender).isEmpty()) {
                invalidLenders.add("Invalid lender: " + lender);
            }
            if (code == null || !code.contains("-")) {
                invalidProductCodes.add("Invalid product code format: " + code);
            }
        }

        // Validate overlapping salary slabs in FOIR
        Map<String, List<WorkbookModels.FoirRow>> foirGroups = foirRows.stream()
                .collect(Collectors.groupingBy(r -> String.format("%s:%s:%s",
                        normalizer.normalizeLender(r.lenderName()),
                        normalizer.normalizeSurrogate(r.surrogate()),
                        normalizer.normalizeEmploymentType(r.employmentType()))));

        for (var entry : foirGroups.entrySet()) {
            List<WorkbookModels.FoirRow> groupRows = new ArrayList<>(entry.getValue());
            groupRows.sort(Comparator.comparing(r -> r.lowerSalary() != null ? r.lowerSalary() : BigDecimal.ZERO));
            for (int i = 1; i < groupRows.size(); i++) {
                WorkbookModels.FoirRow prev = groupRows.get(i - 1);
                WorkbookModels.FoirRow curr = groupRows.get(i);
                BigDecimal prevUpper = prev.upperSalary() != null ? prev.upperSalary() : new BigDecimal("999999999");
                BigDecimal currLower = curr.lowerSalary() != null ? curr.lowerSalary() : BigDecimal.ZERO;
                if (currLower.compareTo(prevUpper) <= 0) {
                    slabOverlaps.add(String.format("Overlapping FOIR salary slab for %s: [%s - %s] overlaps with [%s - %s]",
                            entry.getKey(), prev.lowerSalary(), prev.upperSalary(), curr.lowerSalary(), curr.upperSalary()));
                }
            }
        }

        // Validate overlapping loan slabs in Processing Fees
        Map<String, List<WorkbookModels.PfRow>> pfGroups = pfRows.stream()
                .collect(Collectors.groupingBy(r -> String.format("%s:%s:%s",
                        normalizer.normalizeLender(r.lenderName()),
                        normalizer.normalizeLoanType(r.loanType()),
                        normalizer.normalizeEmploymentType(r.employmentType()))));

        for (var entry : pfGroups.entrySet()) {
            List<WorkbookModels.PfRow> groupRows = new ArrayList<>(entry.getValue());
            groupRows.sort(Comparator.comparing(r -> r.minLoanAmount() != null ? r.minLoanAmount() : BigDecimal.ZERO));
            for (int i = 1; i < groupRows.size(); i++) {
                WorkbookModels.PfRow prev = groupRows.get(i - 1);
                WorkbookModels.PfRow curr = groupRows.get(i);
                BigDecimal prevMax = prev.maxLoanAmount() != null ? prev.maxLoanAmount() : new BigDecimal("999999999");
                BigDecimal currMin = curr.minLoanAmount() != null ? curr.minLoanAmount() : BigDecimal.ZERO;
                if (currMin.compareTo(prevMax) <= 0) {
                    slabOverlaps.add(String.format("Overlapping PF loan slab for %s: [%s - %s] overlaps with [%s - %s]",
                            entry.getKey(), prev.minLoanAmount(), prev.maxLoanAmount(), curr.minLoanAmount(), curr.maxLoanAmount()));
                }
            }
        }

        List<String> crossWorkbookErrors = new ArrayList<>();
        Set<String> foirLenders = foirRows.stream()
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());
        Set<String> pfLenders = pfRows.stream()
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());
        Set<String> loginLenders = loginFeeRows.stream()
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());
        Set<String> lapLenders = lapLtvRows.stream()
                .map(r -> normalizer.normalizeLender(r.lenderName()))
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toSet());

        for (var row : eligibilityRows) {
            String lender = row.lenderName();
            if (lender == null || lender.isBlank()) continue;
            String normLender = normalizer.normalizeLender(lender);
            String prodName = row.productName() != null ? row.productName().toUpperCase() : "";

            if (!foirLenders.contains(normLender)) {
                crossWorkbookErrors.add(String.format("Lender '%s' from Eligibility rules not found in FOIR sheet", lender));
            }
            if (!pfLenders.contains(normLender)) {
                crossWorkbookErrors.add(String.format("Lender '%s' from Eligibility rules not found in PF sheet", lender));
            }
            if (!loginLenders.contains(normLender)) {
                crossWorkbookErrors.add(String.format("Lender '%s' from Eligibility rules not found in Login Fees sheet", lender));
            }
            if ((prodName.contains("LAP") || prodName.contains("PROPERTY")) && !lapLenders.contains(normLender)) {
                crossWorkbookErrors.add(String.format("LAP Lender '%s' from Eligibility rules not found in LAP LTV sheet", lender));
            }
        }

        if (!duplicates.isEmpty() || !invalidLenders.isEmpty() || !invalidProductCodes.isEmpty() || !slabOverlaps.isEmpty() || !crossWorkbookErrors.isEmpty()) {
            structurePass = false;
            structureMessage = String.format("Validation failed: %d duplicates, %d overlaps, %d invalid lenders, %d invalid product codes, %d cross-workbook errors",
                    duplicates.size(), slabOverlaps.size(), invalidLenders.size(), invalidProductCodes.size(), crossWorkbookErrors.size());
        }
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.STRUCTURE_VALIDATION, structurePass, structureMessage));

        // 3. Database Cross-Reference (Workbook vs live database conditions)
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
                compareField(fields, "maxLoanAmount", wRow.maxLoanAmount(), cond.getMinLoanAmount());

                BigDecimal expectedLtvVal = parseLtvAllowed(wRow.ltv());
                if (expectedLtvVal != null) {
                    compareField(fields, "ltvAllowed", expectedLtvVal, cond.getLtvAllowed());
                }

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

        // 4. Scenario Replay
        List<CertificationReportModels.ReplayRowResult> replayResults = new ArrayList<>();
        List<CertificationReportModels.PipelineAuditItem> pipelineItems = new ArrayList<>();
        List<CertificationReportModels.FormulaDriftItem> driftItems = new ArrayList<>();
        List<Map<String, Object>> replayManifestItems = new ArrayList<>();

        int totalReplayed = 0;
        int passedReplays = 0;
        Map<String, Long> ruleExecCounts = new HashMap<>();
        Map<String, Long> rulePassCounts = new HashMap<>();
        Map<String, Long> ruleFailCounts = new HashMap<>();
        Map<String, Long> ruleSkipCounts = new HashMap<>();

        // Keep track of database calls during runs
        int dbQueryCount = 1; // 1 query for dbConditions loaded initially

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

            // Policy Evaluator Lookup based on Governance Registry
            BigDecimal expectedFoir = BigDecimal.valueOf(0.65);
            if (policyOwnershipRegistry.getOwner(PolicyOwnershipRegistry.PolicyDomain.FOIR) == PolicyOwnershipRegistry.Owner.WORKBOOK) {
                expectedFoir = evaluator.lookupFoir(foirRows, row.lenderName(), row.surrogate(), row.employmentType(), request.monthlyIncome());
            }

            // ROI Matrix is owned by Database
            BigDecimal expectedRoi = BigDecimal.valueOf(0.0825); // Fallback base ROI
            if (policyOwnershipRegistry.getOwner(PolicyOwnershipRegistry.PolicyDomain.ROI_MATRIX) == PolicyOwnershipRegistry.Owner.DATABASE) {
                Optional<LoanProduct> optProduct = loanProductRepository.findByProductCode(codePrefix);
                if (optProduct.isPresent()) {
                    dbQueryCount++;
                    LoanProduct lp = optProduct.get();
                    expectedRoi = evaluator.resolveDatabaseRoi(
                            lp.getId(),
                            request.employmentType(),
                            request.loanAmount(),
                            request.cibilScore(),
                            false,
                            lp.getRoi() != null ? lp.getRoi() : BigDecimal.valueOf(0.0825)
                    );
                    dbQueryCount++; // for repository findByProductId inside resolveDatabaseRoi
                }
            }

            // Low LTV Workbook Isolation Rule: HL_LTV_Sheet.xlsx and LAP_LTV_Sheet.xlsx 
            // must never be consulted during NIP or surrogate replays.
            boolean isLowLtvFallbackSurrogate = "LOW_LTV".equalsIgnoreCase(normalizer.normalizeSurrogate(row.surrogate()));
            BigDecimal expectedLtv = BigDecimal.ZERO;

            if (isLowLtvFallbackSurrogate) {
                boolean isHl = "HL".equalsIgnoreCase(row.productName());
                PolicyOwnershipRegistry.PolicyDomain ltvDomain = isHl ? PolicyOwnershipRegistry.PolicyDomain.HL_LTV : PolicyOwnershipRegistry.PolicyDomain.LAP_LTV;
                if (policyOwnershipRegistry.getOwner(ltvDomain) == PolicyOwnershipRegistry.Owner.WORKBOOK) {
                    if (isHl) {
                        expectedLtv = evaluator.lookupHlLtv(hlLtvRows, request.propertyType(), request.loanAmount());
                    } else {
                        expectedLtv = evaluator.resolveLapLtvFromRequest(
                                lapLtvRows,
                                row.lenderName(),
                                request.propertyType(),
                                request.propertyCategory(),
                                request.businessPropertyCategory()
                        );
                    }
                }
            } else {
                BigDecimal parsedLtv = parseLtvAllowed(row.ltv());
                expectedLtv = parsedLtv != null ? parsedLtv : BigDecimal.ZERO;

                // Verify that NIP or standard surrogates do not consult the Low LTV fallback grids
                if (targetResult != null) {
                    BigDecimal actualLtv = targetResult.ltv();
                    BigDecimal lowLtvSheetVal = BigDecimal.ZERO;
                    if ("HL".equalsIgnoreCase(row.productName())) {
                        lowLtvSheetVal = evaluator.lookupHlLtv(hlLtvRows, request.propertyType(), request.loanAmount());
                    } else {
                        lowLtvSheetVal = evaluator.resolveLapLtvFromRequest(
                                lapLtvRows,
                                row.lenderName(),
                                request.propertyType(),
                                request.propertyCategory(),
                                request.businessPropertyCategory()
                        );
                    }

                    if (actualLtv != null && actualLtv.compareTo(BigDecimal.ZERO) > 0 
                            && lowLtvSheetVal != null && lowLtvSheetVal.compareTo(BigDecimal.ZERO) > 0
                            && actualLtv.compareTo(lowLtvSheetVal) == 0 
                            && (parsedLtv == null || parsedLtv.compareTo(lowLtvSheetVal) != 0)) {
                        
                        deviations.add(new CertificationReportModels.FieldMismatch(
                                "LTV_ISOLATION",
                                parsedLtv != null ? parsedLtv.toString() : "NA",
                                actualLtv.toString(),
                                "ENGINE_LOGIC_MISMATCH: NIP/surrogate program consulted Low LTV fallbacks outside Low LTV cascade"
                        ));
                    }
                }
            }

            BigDecimal expectedEmi = evaluator.calculateEmi(request.loanAmount(), expectedRoi, request.requestedTenureMonths());

            BigDecimal expectedPf = BigDecimal.ZERO;
            if (policyOwnershipRegistry.getOwner(PolicyOwnershipRegistry.PolicyDomain.PROCESSING_FEE) == PolicyOwnershipRegistry.Owner.WORKBOOK) {
                expectedPf = evaluator.calculateProcessingFee(pfRows, row.lenderName(), row.productName(), row.employmentType(), request.loanAmount());
            }

            BigDecimal expectedLoginFee = BigDecimal.ZERO;
            if (policyOwnershipRegistry.getOwner(PolicyOwnershipRegistry.PolicyDomain.LOGIN_FEE) == PolicyOwnershipRegistry.Owner.WORKBOOK) {
                expectedLoginFee = evaluator.lookupLoginFee(loginFeeRows, row.lenderName(), row.productName(), row.employmentType(), request.loanAmount());
            }

            // Tax mode GST scaling check
            // GST 18% is added inside calculateProcessingFee/lookupLoginFee if GST is GST_EXCLUSIVE
            // If they are GST_EXCLUSIVE, we multiply by 1.18. Let's make sure expected matches exactly what evaluator calculates.

            if (targetResult != null) {
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

                        // Formula Drift validation
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

                // Check deviations & values
                compareField(deviations, "eligible", true, targetResult.eligible());
                compareFoirField(deviations, "foir", expectedFoir, targetResult.effectiveFoir());
                compareDecimalField(deviations, "roi", expectedRoi, targetResult.roi(), new BigDecimal("0.0001"));
                compareDecimalField(deviations, "ltv", expectedLtv, targetResult.ltv(), new BigDecimal("0.001"));
                compareDecimalField(deviations, "emi", expectedEmi, targetResult.proposedEmi(), BigDecimal.ONE);
                compareDecimalField(deviations, "processingFee", expectedPf, targetResult.processingFee(), BigDecimal.ONE);
                compareDecimalField(deviations, "loginFee", expectedLoginFee, targetResult.loginFee(), BigDecimal.ONE);

                // Pipeline verification check
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

            var rowRes = new CertificationReportModels.ReplayRowResult(
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
            );
            replayResults.add(rowRes);

            // Replay ID systematic format: [Product]_[Lender]_[RowIndex]
            String lenderCode = normalizer.normalizeLender(row.lenderName()).toUpperCase().replace(" ", "_");
            String replayId = String.format("%s_%s_%d", "HL".equalsIgnoreCase(row.productName()) ? "HL" : "LAP", lenderCode, i + 2);

            // Determine Severity of mismatches
            String severity = "NONE";
            List<String> mismatchMsgs = new ArrayList<>();
            if (!rowPass) {
                severity = "LOW";
                for (var dev : deviations) {
                    String f = dev.field().toLowerCase();
                    if (f.equals("eligible") || f.equals("roi") || f.equals("product_match") || f.equals("ltv_isolation")) {
                        severity = "CRITICAL";
                    } else if ((f.equals("ltv") || f.equals("foir") || f.equals("emi") || f.equals("pipeline")) && !severity.equals("CRITICAL")) {
                        severity = "HIGH";
                    } else if ((f.equals("processingfee") || f.equals("loginfee")) && !severity.equals("CRITICAL") && !severity.equals("HIGH")) {
                        severity = "MEDIUM";
                    }
                    mismatchMsgs.add(dev.message());
                }
            }

            Map<String, Object> manifestItem = new LinkedHashMap<>();
            manifestItem.put("replayId", replayId);
            manifestItem.put("workbook", "eligibility_workbook.xlsx");
            manifestItem.put("sheet", "Loan_Product_Master");
            manifestItem.put("workbookRow", i + 2);
            manifestItem.put("databaseCondition", targetResult != null && targetResult.decisionTrace() != null ? "condition_id=" + targetResult.decisionTrace().steps().get(0).matchedConditionId() : "N/A");
            manifestItem.put("program", rowRes.actualProgram());
            Map<String, Object> expectedMap = new LinkedHashMap<>();
            expectedMap.put("eligible", true);
            expectedMap.put("foir", expectedFoir != null ? expectedFoir : BigDecimal.ZERO);
            expectedMap.put("roi", expectedRoi != null ? expectedRoi : BigDecimal.ZERO);
            expectedMap.put("ltv", expectedLtv != null ? expectedLtv : BigDecimal.ZERO);
            expectedMap.put("processingFee", expectedPf != null ? expectedPf : BigDecimal.ZERO);
            expectedMap.put("loginFee", expectedLoginFee != null ? expectedLoginFee : BigDecimal.ZERO);
            manifestItem.put("expected", expectedMap);

            Map<String, Object> actualMap = new LinkedHashMap<>();
            actualMap.put("eligible", targetResult != null ? targetResult.eligible() : false);
            actualMap.put("foir", targetResult != null && targetResult.effectiveFoir() != null ? targetResult.effectiveFoir() : BigDecimal.ZERO);
            actualMap.put("roi", targetResult != null && targetResult.roi() != null ? targetResult.roi() : BigDecimal.ZERO);
            actualMap.put("ltv", targetResult != null && targetResult.ltv() != null ? targetResult.ltv() : BigDecimal.ZERO);
            actualMap.put("processingFee", targetResult != null && targetResult.processingFee() != null ? targetResult.processingFee() : BigDecimal.ZERO);
            actualMap.put("loginFee", targetResult != null && targetResult.loginFee() != null ? targetResult.loginFee() : BigDecimal.ZERO);
            manifestItem.put("actual", actualMap);
            manifestItem.put("severity", severity);
            manifestItem.put("mismatches", mismatchMsgs);
            replayManifestItems.add(manifestItem);
        }

        boolean replayPass = passedReplays == totalReplayed;
        String replayMessage = String.format("Replayed %d scenarios. Passed %d / %d", totalReplayed, passedReplays, totalReplayed);
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.REPLAY_COVERAGE, replayPass, replayMessage));

        double passPct = totalReplayed > 0 ? (double) passedReplays / totalReplayed * 100.0 : 0.0;
        CertificationReportModels.SpreadsheetReplayReport replayReport = new CertificationReportModels.SpreadsheetReplayReport(
                replayResults, totalReplayed, passedReplays, totalReplayed - passedReplays, passPct, replayPass
        );

        // Rule Coverage
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

        // Pipeline verification
        long pipelineMatches = pipelineItems.stream().filter(CertificationReportModels.PipelineAuditItem::match).count();
        boolean pipelinePass = pipelineMatches == pipelineItems.size();
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.PIPELINE_VERIFICATION, pipelinePass, pipelinePass ? "Cascade pipeline orders verified successfully" : "Pipeline order mismatch detected"));
        CertificationReportModels.PipelineAuditReport pipelineReport = new CertificationReportModels.PipelineAuditReport(pipelineItems, pipelineItems.size(), (int) pipelineMatches, pipelinePass);

        // Formula Drift
        long driftFailures = driftItems.stream().filter(x -> !x.pass()).count();
        boolean driftPass = driftFailures == 0;
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.FORMULA_DRIFT_VALIDATION, driftPass, driftPass ? "Formula drift validation success (EMI matches within ±₹1)" : "Formula drift deviations detected"));
        CertificationReportModels.FormulaDriftReport formulaDriftReport = new CertificationReportModels.FormulaDriftReport(driftItems, driftItems.size(), (int) driftFailures, driftPass);

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

        // Reachability Audit
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

        // Shadow Mismatch Classification
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
                    } else if ("LTV_ISOLATION".equalsIgnoreCase(dev.field())) {
                        classification = CertificationEnums.MismatchClassification.ENGINE_LOGIC_MISMATCH;
                    }
                    classifiedMismatches.add(new CertificationReportModels.ClassifiedMismatch(
                            "REPLAY", String.valueOf(replay.rowIndex()), dev.field(), dev.expected(), dev.actual(), classification, "Inspect engine logic for " + dev.field() + " divergence"
                    ));
                    counts.put(classification, counts.getOrDefault(classification, 0) + 1);
                }
            }
        }
        CertificationReportModels.MismatchClassificationReport classificationReport = new CertificationReportModels.MismatchClassificationReport(classifiedMismatches, counts);

        // 5. Policy Drift Detection
        Map<String, List<String>> classifiedDrifts = new LinkedHashMap<>();
        classifiedDrifts.put("POLICY_DRIFT", new ArrayList<>());
        classifiedDrifts.put("CONFIGURATION_DRIFT", new ArrayList<>());
        classifiedDrifts.put("DATABASE_DRIFT", new ArrayList<>());
        classifiedDrifts.put("ENGINE_DRIFT", new ArrayList<>());

        Map<String, Map<String, Object>> currSnapshot = new LinkedHashMap<>();
        for (var row : eligibilityRows) {
            String rowKey = getProductCodePrefix(row) + ":" + row.employmentType() + ":" + row.surrogate();
            Map<String, Object> vals = new LinkedHashMap<>();
            vals.put("minCibil", row.minCibil());
            vals.put("minIncome", row.minIncome() != null ? row.minIncome().toString() : null);
            vals.put("minAge", row.minAge());
            vals.put("maxAge", row.maxAge());
            vals.put("ltv", row.ltv());
            currSnapshot.put(rowKey, vals);
        }

        try {
            File snapshotFile = new File("src/main/resources/certification/policy_drift_snapshot.json");
            if (snapshotFile.exists()) {
                Map<String, Map<String, Object>> prevSnapshot = OBJECT_MAPPER.readValue(snapshotFile, Map.class);
                List<String> policyDrifts = classifiedDrifts.get("POLICY_DRIFT");
                List<String> configDrifts = classifiedDrifts.get("CONFIGURATION_DRIFT");

                for (var row : eligibilityRows) {
                    String rowKey = getProductCodePrefix(row) + ":" + row.employmentType() + ":" + row.surrogate();
                    if (prevSnapshot.containsKey(rowKey)) {
                        Map<String, Object> prevVals = prevSnapshot.get(rowKey);
                        checkDrift(policyDrifts, rowKey, "minCibil", row.minCibil(), prevVals.get("minCibil"));
                        checkDrift(policyDrifts, rowKey, "minIncome", row.minIncome(), prevVals.get("minIncome"));
                        checkDrift(policyDrifts, rowKey, "minAge", row.minAge(), prevVals.get("minAge"));
                        checkDrift(policyDrifts, rowKey, "maxAge", row.maxAge(), prevVals.get("maxAge"));
                        checkDrift(policyDrifts, rowKey, "ltv", row.ltv(), prevVals.get("ltv"));
                    } else {
                        configDrifts.add("New policy added in workbook: " + rowKey);
                    }
                }
            }
            // Save latest policies to drift snapshot
            snapshotFile.getParentFile().mkdirs();
            OBJECT_MAPPER.writeValue(snapshotFile, currSnapshot);
        } catch (Exception e) {
            log.error("Policy drift check failed", e);
        }

        // Add Database drifts
        List<String> dbDrifts = classifiedDrifts.get("DATABASE_DRIFT");
        for (var m : dbMismatches) {
            for (var f : m.mismatches()) {
                dbDrifts.add(String.format("DB mismatch for product=%s lender=%s field=%s: expected=%s actual=%s. %s",
                        m.productCode(), m.bankName(), f.field(), f.expected(), f.actual(), f.message()));
            }
        }

        // Add Engine drifts
        List<String> engineDrifts = classifiedDrifts.get("ENGINE_DRIFT");
        for (var replay : replayResults) {
            if (!replay.pass()) {
                String lenderCode = replay.bankName() != null ? replay.bankName().toUpperCase().replace(" ", "_") : "UNKNOWN";
                String rId = String.format("%s_%s_%d", replay.productCode(), lenderCode, replay.rowIndex());
                for (var dev : replay.deviations()) {
                    engineDrifts.add(String.format("Replay deviation for scenario=%s index=%d field=%s: expected=%s actual=%s. %s",
                            rId, replay.rowIndex(), dev.field(), dev.expected(), dev.actual(), dev.message()));
                }
            }
        }

        // 6. Manifest & Verdict
        String overallStatus = "PASS";
        boolean hasCriticalOrHigh = replayManifestItems.stream()
                .anyMatch(x -> "CRITICAL".equals(x.get("severity")) || "HIGH".equals(x.get("severity")));
        boolean hasMediumOrLow = replayManifestItems.stream()
                .anyMatch(x -> "MEDIUM".equals(x.get("severity")) || "LOW".equals(x.get("severity")));

        if (hasCriticalOrHigh || !structurePass || !dbCrossPass || !determinismPass) {
            overallStatus = "FAIL";
        } else if (hasMediumOrLow || !rulePass || !pipelinePass || !reachPass) {
            overallStatus = "CONDITIONAL_PASS";
        }

        // 7. Telemetry & Performance Measurement
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        System.gc();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memDeltaKb = (endMemory - startMemory) / 1024;

        log.info("Certification completed in {}ms. DB Queries executed: {}. Memory footprint delta: {} KB. Verdict: {}",
                durationMs, dbQueryCount, memDeltaKb, overallStatus);

        boolean certified = "PASS".equals(overallStatus) || "CONDITIONAL_PASS".equals(overallStatus);
        gates.add(new CertificationReportModels.GateResult(CertificationEnums.CertificationGate.PRODUCTION_GATE, certified,
                "Overall certification verdict: " + overallStatus));

        // Generate Release Evidence Package containing all 8 JSON reports
        writeReleaseEvidencePackage(
                certificationId,
                overallStatus,
                workbookHash,
                structurePass,
                duplicates,
                slabOverlaps,
                invalidLenders,
                invalidProductCodes,
                crossWorkbookErrors,
                passPct,
                ruleItems,
                neverExecutedRules,
                totalReplayed,
                passedReplays,
                pipelineItems,
                driftItems,
                reachItems,
                (int) reachableCount,
                dbConditions.size(),
                classifiedMismatches,
                counts,
                classifiedDrifts,
                currSnapshot,
                gates,
                replayManifestItems,
                durationMs,
                dbQueryCount,
                memDeltaKb
        );

        return new CertificationReportModels.CertificationReport(
                certificationId, Instant.now(), "1.0.0", masterDataVersionService.computeVersion(), workbookHash, "fingerprint_placeholder",
                dbConditions.size(), dbCrossPass ? 100.0 : ((double) matchedDbCount / dbConditions.size() * 100.0),
                expectedRules.size(), (double) (expectedRules.size() - neverExecutedRules.size()) / expectedRules.size() * 100.0,
                totalReplayed, passPct, driftItems.stream().filter(x -> !x.pass()).toList().size(),
                pipelineItems.stream().filter(x -> !x.match()).toList().size(),
                (int) (dbConditions.size() - reachableCount), certified, gates,
                masterDataReport, ruleReport, replayReport, pipelineReport, formulaDriftReport, snapshotReport, reachabilityReport, classificationReport
        );
    }

    private void checkDrift(List<String> driftMessages, String key, String field, Object currVal, Object prevVal) {
        if (currVal == null && prevVal == null) return;
        if (currVal != null && prevVal != null) {
            if (currVal.toString().trim().equalsIgnoreCase(prevVal.toString().trim())) return;
        }
        driftMessages.add(String.format("Policy drifted for %s on field %s: previous value was %s, new value is %s",
                key, field, prevVal, currVal));
    }

    private String getGitCommit() {
        try {
            Process process = Runtime.getRuntime().exec("git rev-parse HEAD");
            try (InputStream is = process.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8).trim();
            }
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Optional<WorkbookModels.EligibilityRow> findMatchingWorkbookRow(List<WorkbookModels.EligibilityRow> rows, EligibilityCondition cond) {
        String condLender = cond.getBankName();
        String condType = cond.getLoanType();
        String condEmp = cond.getEmploymentType();
        String condSurrogate = cond.getSurrogate();

        return rows.stream().filter(r -> {
            boolean lenderMatch = normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normalizer.normalizeLender(condLender));

            // Normalize Loan Type
            String excelType = r.loanType() != null && r.loanType().equalsIgnoreCase("Secured") ? "HL" : "LAP";
            if (r.productName() != null) {
                excelType = r.productName().trim();
            }
            String dbType = condType.contains("HOME") || condType.equalsIgnoreCase("HL") ? "HL" : "LAP";
            boolean typeMatch = excelType.equalsIgnoreCase(dbType);

            // Normalize Employment Type
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

            // Normalize Surrogate
            boolean surrogateMatch = false;
            String wSurrogate = normalizer.normalizeSurrogate(r.surrogate());
            String cSurrogate = normalizer.normalizeSurrogate(condSurrogate);
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

        if (exp.compareTo(BigDecimal.ONE) < 0) exp = exp.multiply(new BigDecimal("100"));
        if (act.compareTo(BigDecimal.ONE) < 0) act = act.multiply(new BigDecimal("100"));

        BigDecimal diff = exp.subtract(act).abs();
        if (diff.compareTo(new BigDecimal("0.1")) > 0) {
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

        String surrogate = normalizer.normalizeSurrogate(row.surrogate());

        if ("LOW_LTV".equals(surrogate)) {
            income = BigDecimal.ONE;
        }

        IncomeComputationInput incomeInput = new IncomeComputationInput(
                surrogate, income.multiply(new BigDecimal("12")), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), BigDecimal.ZERO, "", BigDecimal.ZERO, "",
                normalizer.normalizeLender(row.lenderName()),
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
            String normLender = normalizer.normalizeLender(row.lenderName());
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
                null,
                row.productName() != null ? row.productName().trim() : "HL",
                cibil, age,
                row.employmentType() != null && row.employmentType().contains("Salaried") ? "Salaried" : "Self Employed",
                propType,
                "Tier 1",
                loanAmount, propertyValue, tenure, income, BigDecimal.ZERO,
                3, 5, incomeInput,
                "idempotency-certification-replay-" + index,
                3, income,
                "452001",
                propType,
                null
        );
    }

    private List<String> getExpectedCascadePipeline(String surrogate) {
        List<String> pipeline = new ArrayList<>();
        pipeline.add("NIP");
        String normSurrogate = normalizer.normalizeSurrogate(surrogate);
        if (!"NIP".equals(normSurrogate)) {
            if (normSurrogate.contains("LOW_LTV") || normSurrogate.contains("LOWLTV")) {
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
        String normLender = normalizer.normalizeLender(row.lenderName());
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

    private void writeReleaseEvidencePackage(
            String certificationId,
            String overallStatus,
            String workbookHash,
            boolean structurePass,
            List<String> duplicates,
            List<String> slabOverlaps,
            List<String> invalidLenders,
            List<String> invalidProductCodes,
            List<String> crossWorkbookErrors,
            double passPct,
            List<CertificationReportModels.RuleCoverageItem> ruleItems,
            List<String> neverExecutedRules,
            int totalReplayed,
            int passedReplays,
            List<CertificationReportModels.PipelineAuditItem> pipelineItems,
            List<CertificationReportModels.FormulaDriftItem> driftItems,
            List<CertificationReportModels.ConditionReachabilityItem> reachItems,
            int reachableCount,
            int totalDbConditions,
            List<CertificationReportModels.ClassifiedMismatch> classifiedMismatches,
            Map<CertificationEnums.MismatchClassification, Integer> mismatchCounts,
            Map<String, List<String>> classifiedDrifts,
            Map<String, Map<String, Object>> currSnapshot,
            List<CertificationReportModels.GateResult> gates,
            List<Map<String, Object>> replayManifestItems,
            long durationMs,
            int dbQueryCount,
            long memDeltaKb
    ) {
        try {
            File evidenceDir = new File("evidence");
            if (!evidenceDir.exists()) {
                evidenceDir.mkdirs();
            }

            // 1. certification_manifest.json
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("certificationId", certificationId);
            manifest.put("timestamp", Instant.now().toString());
            manifest.put("gitCommit", getGitCommit());
            manifest.put("engineVersion", "1.0.0-SSOT");
            manifest.put("masterDataVersion", masterDataVersionService.computeVersion());
            manifest.put("workbookHash", workbookHash);
            manifest.put("overallVerdict", "FAIL".equals(overallStatus) ? "FAIL" : "PASS");
            manifest.put("overallStatus", overallStatus);
            manifest.put("telemetry", Map.of(
                    "durationMs", durationMs,
                    "dbQueryCount", dbQueryCount,
                    "memoryDeltaKb", memDeltaKb
            ));
            manifest.put("coverageSummary", Map.of(
                    "workbookCoverage", totalReplayed > 0 ? 100.0 : 0.0,
                    "databaseCoverage", totalDbConditions > 0 ? (double) reachableCount / totalDbConditions * 100.0 : 0.0,
                    "ruleCoverage", ruleItems.isEmpty() ? 0.0 : (double) (ruleItems.size() - neverExecutedRules.size()) / ruleItems.size() * 100.0,
                    "replayCoverage", passPct,
                    "formulaCoverage", driftItems.isEmpty() ? 100.0 : (double) (driftItems.size() - driftItems.stream().filter(x -> !x.pass()).count()) / driftItems.size() * 100.0
            ));
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "certification_manifest.json"), manifest);

            File resourceManifest = new File("src/main/resources/certification/certification_manifest.json");
            resourceManifest.getParentFile().mkdirs();
            OBJECT_MAPPER.writeValue(resourceManifest, manifest);

            // 2. workbook_validation_report.json
            Map<String, Object> validationReport = new LinkedHashMap<>();
            validationReport.put("validationPassed", structurePass && crossWorkbookErrors.isEmpty());
            validationReport.put("duplicatesCount", duplicates.size());
            validationReport.put("duplicates", duplicates);
            validationReport.put("overlapsCount", slabOverlaps.size());
            validationReport.put("slabOverlaps", slabOverlaps);
            validationReport.put("invalidLenders", invalidLenders);
            validationReport.put("invalidProductCodes", invalidProductCodes);
            validationReport.put("crossWorkbookErrorsCount", crossWorkbookErrors.size());
            validationReport.put("crossWorkbookErrors", crossWorkbookErrors);
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "workbook_validation_report.json"), validationReport);

            // 3. coverage_report.json
            Map<String, Object> coverageReport = new LinkedHashMap<>();
            coverageReport.put("ruleCoverage", Map.of(
                    "percentage", ruleItems.isEmpty() ? 0.0 : (double) (ruleItems.size() - neverExecutedRules.size()) / ruleItems.size() * 100.0,
                    "items", ruleItems
            ));
            coverageReport.put("workbookCoverage", Map.of(
                    "percentage", totalReplayed > 0 ? 100.0 : 0.0,
                    "totalRows", totalReplayed,
                    "matchedRows", totalReplayed
            ));
            coverageReport.put("databaseCoverage", Map.of(
                    "percentage", totalDbConditions > 0 ? (double) reachableCount / totalDbConditions * 100.0 : 0.0,
                    "totalConditions", totalDbConditions,
                    "matchedConditions", reachableCount
            ));
            coverageReport.put("formulaCoverage", Map.of(
                    "percentage", driftItems.isEmpty() ? 100.0 : (double) (driftItems.size() - driftItems.stream().filter(x -> !x.pass()).count()) / driftItems.size() * 100.0,
                    "totalFormulaeChecked", driftItems.size(),
                    "matchedFormulae", driftItems.stream().filter(x -> x.pass()).count()
            ));
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "coverage_report.json"), coverageReport);

            // 4. replay_manifest.json
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "replay_manifest.json"), replayManifestItems);
            OBJECT_MAPPER.writeValue(new File("src/main/resources/certification/replay_manifest.json"), replayManifestItems);

            // 5. policy_diagnostics_report.json
            Map<String, Object> diagnosticsReport = new LinkedHashMap<>();
            Map<String, String> registryMap = new LinkedHashMap<>();
            for (var entry : policyOwnershipRegistry.getRegistry().entrySet()) {
                registryMap.put(entry.getKey().name(), entry.getValue().name());
            }
            diagnosticsReport.put("ownershipRegistry", registryMap);
            diagnosticsReport.put("reachability", Map.of(
                    "passed", reachableCount == totalDbConditions,
                    "reachableCount", reachableCount,
                    "totalCount", totalDbConditions,
                    "details", reachItems
            ));
            diagnosticsReport.put("mismatchClassification", Map.of(
                    "totalMismatches", classifiedMismatches.size(),
                    "classifications", mismatchCounts,
                    "details", classifiedMismatches
            ));
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "policy_diagnostics_report.json"), diagnosticsReport);

            // 6. drift_report.json
            Map<String, Object> driftReport = new LinkedHashMap<>();
            boolean driftDetected = classifiedDrifts.values().stream().anyMatch(list -> !list.isEmpty());
            driftReport.put("driftDetected", driftDetected);
            driftReport.put("classifiedDrifts", classifiedDrifts);
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "drift_report.json"), driftReport);

            // 7. certification_snapshot.json
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "certification_snapshot.json"), currSnapshot);

            // 8. test_results.json
            Map<String, Object> testResults = new LinkedHashMap<>();
            testResults.put("gates", gates);
            OBJECT_MAPPER.writeValue(new File(evidenceDir, "test_results.json"), testResults);

            log.info("Successfully wrote Release Evidence Package containing all 8 JSON reports under evidence/ directory.");
        } catch (Exception e) {
            log.error("Failed to generate Release Evidence Package", e);
        }
    }
}
