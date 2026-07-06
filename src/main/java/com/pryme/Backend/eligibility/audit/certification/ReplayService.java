package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.audit.DecisionStatus;
import com.pryme.Backend.eligibility.audit.DecisionStep;
import com.pryme.Backend.eligibility.audit.DecisionTrace;
import com.pryme.Backend.eligibility.audit.FormulaTrace;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.audit.RuleEvaluation;
import com.pryme.Backend.eligibility.policy.engine.PolicyProductMatcher;
import com.pryme.Backend.eligibility.policy.model.*;
import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 🏎️ Executes replay scenarios against the eligibility engine and compares results with the independent evaluator.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReplayService {

    private final EligibilityEngineService engine;
    private final IndependentPolicyEvaluator evaluator;
    private final PolicyProductMatcher policyProductMatcher;
    private final CentralizedNormalizer normalizer;
    private final PolicyOwnershipRegistry policyOwnershipRegistry;

    // Track statistics
    private final Map<String, Long> ruleExecCounts = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Long> rulePassCounts = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Long> ruleFailCounts = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Long> ruleSkipCounts = new java.util.concurrent.ConcurrentHashMap<>();
    private final List<CertificationReportModels.FormulaDriftItem> driftItems = java.util.Collections.synchronizedList(new ArrayList<>());

    public List<CertificationReportModels.ReplayRowResult> runReplays(
            List<EligibilityPolicyRule> scenarios,
            CertificationContext context,
            List<LowLtvRule> hlLtvRows,
            List<LowLtvRule> lapLtvRows,
            List<ProcessingFeeRule> pfRows,
            List<LoginFeeRule> loginFeeRows
    ) {
        List<CertificationReportModels.ReplayRowResult> replayResults = new ArrayList<>();
        List<LoanProduct> products = context.getCatalogSnapshot().products();
        PolicyBundle bundle = context.getBundle();

        for (int i = 0; i < scenarios.size(); i++) {
            EligibilityPolicyRule row = scenarios.get(i);
            int rowIdx = i;
            int spreadsheetRow = row.excelRowNumber();

            EmploymentType empType = normalizer.normalizeToEnum(row.employmentType());
            String codePrefix = normalizer.getProductCodePrefix(row.lenderName(), row.productName());

            // 1. Resolve unique product
            LoanProduct targetProduct = null;
            EligibilityRequest request = null;
            try {
                targetProduct = policyProductMatcher.matchUnique(
                        products, row.lenderName(), row.productName(), empType, codePrefix
                );
                request = constructRequestForRow(row, targetProduct, rowIdx);
            } catch (Exception e) {
                // If product is not found or ambiguous, fail the row directly
                context.addDeviation(rowIdx, new CertificationReportModels.FieldMismatch("PRODUCT_MATCH", "Evaluated", "Not Found", e.getMessage()));
                replayResults.add(new CertificationReportModels.ReplayRowResult(
                        spreadsheetRow, row.lenderName(), codePrefix, row.employmentType(), row.surrogate(),
                        true, false,
                        row.surrogate(), "N/A",
                        BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO,
                        new ArrayList<>(context.getRowDeviations().getOrDefault(rowIdx, List.of())), false
                ));
                continue;
            }

            // Execute engine evaluation
            EligibilityResult targetResult = engine.evaluateProductCascaded(targetProduct, request);

            boolean rowPass = true;
            List<CertificationReportModels.FieldMismatch> deviations = new ArrayList<>();

            // 2. LTV Resolution
            boolean isLowLtvFallbackSurrogate = "LOW_LTV".equalsIgnoreCase(normalizer.normalizeSurrogate(row.surrogate()));
            BigDecimal expectedLtv = BigDecimal.ZERO;

            if (isLowLtvFallbackSurrogate) {
                boolean isHl = "HL".equalsIgnoreCase(row.productName());
                PolicyOwnershipRegistry.PolicyDomain ltvDomain = isHl ? PolicyOwnershipRegistry.PolicyDomain.LOW_LTV_HL : PolicyOwnershipRegistry.PolicyDomain.LOW_LTV_LAP;
                if (policyOwnershipRegistry.getSource(ltvDomain) == PolicyOwnershipRegistry.PolicySource.CLIENT_WORKBOOK) {
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

            // 3. FOIR Resolution
            BigDecimal expectedFoir = BigDecimal.valueOf(0.65);
            if (policyOwnershipRegistry.getSource(PolicyOwnershipRegistry.PolicyDomain.FOIR) == PolicyOwnershipRegistry.PolicySource.CLIENT_WORKBOOK) {
                expectedFoir = evaluator.lookupFoir(
                        bundle,
                        row.lenderName(),
                        row.surrogate(),
                        row.employmentType(),
                        request.monthlyIncome(),
                        expectedLtv,
                        targetProduct.getMaxEmiNmiRatio()
                );
            }

            // 4. ROI Resolution
            BigDecimal expectedRoi = BigDecimal.valueOf(0.0825);
            if (policyOwnershipRegistry.getSource(PolicyOwnershipRegistry.PolicyDomain.ROI_MATRIX) == PolicyOwnershipRegistry.PolicySource.DATABASE) {
                expectedRoi = evaluator.resolveDatabaseRoi(
                        targetProduct.getId(),
                        request.employmentType(),
                        request.loanAmount(),
                        request.cibilScore(),
                        false,
                        targetProduct.getRoi() != null ? targetProduct.getRoi() : BigDecimal.valueOf(0.0825),
                        targetProduct.getLenderName()
                );
            }

            // 5. EMI Resolution
            BigDecimal expectedEmi = evaluator.calculateEmi(request.loanAmount(), expectedRoi, request.requestedTenureMonths());

            // 6. Fees Resolution
            BigDecimal expectedPf = BigDecimal.ZERO;
            if (policyOwnershipRegistry.getSource(PolicyOwnershipRegistry.PolicyDomain.PROCESSING_FEE) == PolicyOwnershipRegistry.PolicySource.CLIENT_WORKBOOK) {
                expectedPf = evaluator.calculateProcessingFee(pfRows, row.lenderName(), row.productName(), row.employmentType(), request.loanAmount());
            }

            BigDecimal expectedLoginFee = BigDecimal.ZERO;
            if (policyOwnershipRegistry.getSource(PolicyOwnershipRegistry.PolicyDomain.LOGIN_FEE) == PolicyOwnershipRegistry.PolicySource.CLIENT_WORKBOOK) {
                expectedLoginFee = evaluator.lookupLoginFee(loginFeeRows, row.lenderName(), row.productName(), row.employmentType(), request.loanAmount());
            }

            // Register rule matches for coverage tracking
            context.registerRuleMatch("ELIGIBILITY", CoverageService.makeEligibilityKey(row));

            findMatchingFoirRule(bundle.foirRules(), row.lenderName(), row.surrogate(), row.employmentType(), request.monthlyIncome())
                    .ifPresent(r -> context.registerRuleMatch("FOIR", CoverageService.makeFoirKey(r)));

            findMatchingPfRule(pfRows, row.lenderName(), row.productName(), row.employmentType(), request.loanAmount())
                    .ifPresent(r -> context.registerRuleMatch("PROCESSING_FEE", CoverageService.makePfKey(r)));

            findMatchingLoginFeeRule(loginFeeRows, row.lenderName(), row.productName(), row.employmentType(), request.loanAmount())
                    .ifPresent(r -> context.registerRuleMatch("LOGIN_FEE", CoverageService.makeLoginFeeKey(r)));

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

                        // Formula Drift
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

                // Assertions
                compareField(deviations, "eligible", true, targetResult.eligible());
                compareFoirField(deviations, "foir", expectedFoir, targetResult.effectiveFoir());
                compareDecimalField(deviations, "roi", expectedRoi, targetResult.roi(), new BigDecimal("0.0001"));
                compareDecimalField(deviations, "ltv", expectedLtv, targetResult.ltv(), new BigDecimal("0.001"));
                compareDecimalField(deviations, "emi", expectedEmi, targetResult.proposedEmi(), BigDecimal.ONE);
                compareDecimalField(deviations, "processingFee", expectedPf, targetResult.processingFee(), BigDecimal.ONE);
                compareDecimalField(deviations, "loginFee", expectedLoginFee, targetResult.loginFee(), BigDecimal.ONE);

                // Pipeline verification (filter out SKIPPED steps)
                List<String> expectedPipeline = getExpectedCascadePipeline(row.surrogate());
                List<String> actualPipeline = new ArrayList<>();
                if (trace != null) {
                    for (DecisionStep step : trace.steps()) {
                        actualPipeline.add(step.program().name());
                    }
                }
                boolean pipelineMatch = expectedPipeline.equals(actualPipeline);
                context.addPipelineItem(new CertificationReportModels.PipelineAuditItem(spreadsheetRow, codePrefix, expectedPipeline, actualPipeline, pipelineMatch));
                if (!pipelineMatch) {
                    deviations.add(new CertificationReportModels.FieldMismatch("PIPELINE", expectedPipeline.toString(), actualPipeline.toString(), "Pipeline order mismatch"));
                }
            } else {
                rowPass = false;
                deviations.add(new CertificationReportModels.FieldMismatch("PRODUCT_MATCH", "Evaluated", "Not Found", "Target product " + targetProduct.getProductCode() + " was not evaluated by engine"));
            }

            if (!deviations.isEmpty()) {
                rowPass = false;
                for (var d : deviations) {
                    context.addDeviation(rowIdx, d);
                }
            }

            replayResults.add(new CertificationReportModels.ReplayRowResult(
                    spreadsheetRow, row.lenderName(), codePrefix, row.employmentType(), row.surrogate(),
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

        return replayResults;
    }

    private EligibilityRequest constructRequestForRow(EligibilityPolicyRule row, LoanProduct product, int rowIdx) {
        BigDecimal loanAmount = row.minLoanAmount() != null ? row.minLoanAmount().max(new BigDecimal("2500000")) : new BigDecimal("2500000");
        int cibil = row.minCibil() != null ? row.minCibil() : 750;
        int age = row.minAge() != null ? row.minAge() + 2 : 35;
        BigDecimal income = row.minIncome() != null ? row.minIncome().max(new BigDecimal("50000")) : new BigDecimal("50000");

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
        if (expectedLtvVal == null || expectedLtvVal.compareTo(BigDecimal.ZERO) == 0) {
            expectedLtvVal = new BigDecimal("0.70");
        }
        BigDecimal propertyValue = loanAmount.divide(expectedLtvVal, 2, RoundingMode.HALF_UP);

        return new EligibilityRequest(
                product.getLenderId(),
                row.productName() != null ? row.productName().trim() : "HL",
                cibil, age,
                row.employmentType() != null ? row.employmentType().trim() : "",
                propType,
                "TIER_1",
                loanAmount,
                propertyValue,
                180,
                income,
                BigDecimal.ZERO,
                3,
                3,
                incomeInput,
                "idempotency-certification-replay-" + rowIdx,
                3,
                income,
                "452001",
                "RESIDENTIAL",
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

    private BigDecimal parseLtvAllowed(String ltvStr) {
        if (ltvStr == null || ltvStr.trim().isEmpty() || ltvStr.equalsIgnoreCase("Negative")) {
            return null;
        }
        try {
            ltvStr = ltvStr.replace("%", "").trim();
            BigDecimal val = new BigDecimal(ltvStr);
            if (val.compareTo(BigDecimal.ONE) > 0) {
                val = val.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }
            return val;
        } catch (Exception e) {
            return null;
        }
    }

    private void compareField(List<CertificationReportModels.FieldMismatch> deviations, String field, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            deviations.add(new CertificationReportModels.FieldMismatch(field, String.valueOf(expected), String.valueOf(actual), "Field mismatch"));
        }
    }

    private void compareFoirField(List<CertificationReportModels.FieldMismatch> deviations, String field, BigDecimal expected, BigDecimal actual) {
        if (expected == null && actual == null) return;
        if (expected == null || actual == null || expected.subtract(actual).abs().compareTo(new BigDecimal("0.005")) > 0) {
            deviations.add(new CertificationReportModels.FieldMismatch(field, String.valueOf(expected), String.valueOf(actual), "Field mismatch"));
        }
    }

    private void compareDecimalField(List<CertificationReportModels.FieldMismatch> deviations, String field, BigDecimal expected, BigDecimal actual, BigDecimal tolerance) {
        if (expected == null && actual == null) return;
        if (expected == null || actual == null || expected.subtract(actual).abs().compareTo(tolerance) > 0) {
            deviations.add(new CertificationReportModels.FieldMismatch(field, String.valueOf(expected), String.valueOf(actual), "Field mismatch"));
        }
    }

    private Optional<FoirPolicyRule> findMatchingFoirRule(List<FoirPolicyRule> rules, String lender, String surrogate, String empType, BigDecimal income) {
        String normLender = normalizer.normalizeLender(lender);
        String normSurrogate = normalizer.normalizeSurrogate(surrogate);
        String normEmp = normalizer.normalizeEmploymentType(empType);

        return rules.stream()
                .filter(r -> normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender))
                .filter(r -> normalizer.normalizeSurrogate(r.surrogate()).equalsIgnoreCase(normSurrogate))
                .filter(r -> normalizer.normalizeEmploymentType(r.employmentType()).equalsIgnoreCase(normEmp))
                .filter(r -> {
                    BigDecimal minVal = r.lowerSalary() != null ? r.lowerSalary() : BigDecimal.ZERO;
                    BigDecimal maxVal = r.upperSalary() != null ? r.upperSalary() : new BigDecimal("999999999");
                    return income.compareTo(minVal) >= 0 && income.compareTo(maxVal) <= 0;
                })
                .findFirst();
    }

    private Optional<ProcessingFeeRule> findMatchingPfRule(List<ProcessingFeeRule> rules, String lender, String loanType, String empType, BigDecimal loanAmount) {
        String normLender = normalizer.normalizeLender(lender);
        String normLoan = normalizer.normalizeLoanType(loanType);
        String normEmp = normalizer.normalizeEmploymentType(empType);

        return rules.stream()
                .filter(r -> normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender))
                .filter(r -> normalizer.normalizeLoanType(r.loanType()).equalsIgnoreCase(normLoan))
                .filter(r -> normalizer.normalizeEmploymentType(r.employmentType()).equalsIgnoreCase(normEmp))
                .filter(r -> {
                    BigDecimal minAmt = r.minLoanAmount() != null ? r.minLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = r.maxLoanAmount() != null ? r.maxLoanAmount() : new BigDecimal("999999999");
                    return loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0;
                })
                .findFirst();
    }

    private Optional<LoginFeeRule> findMatchingLoginFeeRule(List<LoginFeeRule> rules, String lender, String loanType, String empType, BigDecimal loanAmount) {
        String normLender = normalizer.normalizeLender(lender);
        String normLoan = normalizer.normalizeLoanType(loanType);
        String normEmp = normalizer.normalizeEmploymentType(empType);

        return rules.stream()
                .filter(r -> normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender))
                .filter(r -> normalizer.normalizeLoanType(r.loanType()).equalsIgnoreCase(normLoan))
                .filter(r -> normalizer.normalizeEmploymentType(r.employmentType()).equalsIgnoreCase(normEmp))
                .filter(r -> {
                    BigDecimal minAmt = r.minLoanAmount() != null ? r.minLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = r.maxLoanAmount() != null ? r.maxLoanAmount() : new BigDecimal("999999999");
                    return loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0;
                })
                .findFirst();
    }

    public Map<String, Long> getRuleExecCounts() { return ruleExecCounts; }
    public Map<String, Long> getRulePassCounts() { return rulePassCounts; }
    public Map<String, Long> getRuleFailCounts() { return ruleFailCounts; }
    public Map<String, Long> getRuleSkipCounts() { return ruleSkipCounts; }
    public List<CertificationReportModels.FormulaDriftItem> getDriftItems() { return driftItems; }
}
