// File: src/main/java/com/pryme/Backend/eligibility/service/EligibilityEngineService.java

package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import com.pryme.Backend.common.repository.PolicyFieldDefinitionRepository;
import com.pryme.Backend.eligibility.dto.ApplicantProfile;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.dto.PreflightRequest;
import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.exception.SurrogatePolicyNotFoundException;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import com.pryme.Backend.eligibility.audit.*;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EligibilityEngineService {

    private final GeneralPolicyPreflightService generalPolicyPreflightService;
    private final LoanProductRepository loanProductRepository;
    private final EligibilityConditionRepository eligibilityConditionRepository;
    private final SurrogateIncomeResolver surrogateIncomeResolver;
    private final PolicyFieldDefinitionRepository policyFieldDefinitionRepository;
    private final SpelExpressionCacheService spelExpressionCacheService;
    private final SimpleEvaluationContext simpleSandboxEvaluationContext;
    private final FinancialComputationEngine financialComputationEngine;
    private final LowLtvSurrogateService lowLtvSurrogateService;
    private final MasterDataVersionService masterDataVersionService;

    private static final String ENGINE_VERSION = "1.0.0";
    private static final BigDecimal DEFAULT_FOIR = new BigDecimal("0.65");

    // ── PROPERTY TYPE NORMALIZATION ──────────────────────────────────────────
    // DB stores categories (RESIDENTIAL, COMMERCIAL, INDUSTRIAL, LAND, PLOT).
    // Frontend sends sub-types (FLAT, HOME, PLOT, SHOP, HOSPITAL, etc.).
    // This map resolves sub-types → categories so the allow-list check works.
    private static final Map<String, String> PROPERTY_SUBTYPE_TO_CATEGORY = Map.ofEntries(
            // Residential sub-types
            Map.entry("FLAT", "RESIDENTIAL"),
            Map.entry("HOME", "RESIDENTIAL"),
            Map.entry("VILLA", "RESIDENTIAL"),
            Map.entry("APARTMENT", "RESIDENTIAL"),
            Map.entry("BUILDER_FLOOR", "RESIDENTIAL"),
            Map.entry("ROW_HOUSE", "RESIDENTIAL"),
            Map.entry("PENTHOUSE", "RESIDENTIAL"),
            Map.entry("RESIDENTIAL", "RESIDENTIAL"),
            Map.entry("READY BUILT PROPERTY", "RESIDENTIAL"),
            Map.entry("READY_BUILT_PROPERTY", "RESIDENTIAL"),
            Map.entry("READY BUILT", "RESIDENTIAL"),
            // Commercial sub-types
            Map.entry("HOSPITAL", "COMMERCIAL"),
            Map.entry("HOSTEL", "COMMERCIAL"),
            Map.entry("RESTAURANTS", "COMMERCIAL"),
            Map.entry("HOTEL", "COMMERCIAL"),
            Map.entry("MARRIAGE_GARDEN", "COMMERCIAL"),
            Map.entry("SCHOOL", "COMMERCIAL"),
            Map.entry("SHOP", "COMMERCIAL"),
            Map.entry("WAREHOUSE", "COMMERCIAL"),
            Map.entry("GODOWN", "COMMERCIAL"),
            Map.entry("OFFICE", "COMMERCIAL"),
            Map.entry("COMMERCIAL", "COMMERCIAL"),
            // Industrial sub-types
            Map.entry("FACTORIES", "INDUSTRIAL"),
            Map.entry("WAREHOUSES", "INDUSTRIAL"),
            Map.entry("DISTRIBUTION_CENTER", "INDUSTRIAL"),
            Map.entry("R_AND_D_FACILITY", "INDUSTRIAL"),
            Map.entry("FLEX_SPACES", "INDUSTRIAL"),
            Map.entry("INDUSTRIAL", "INDUSTRIAL"),
            // Land / Plot (mapped to itself — negative property deny-list handles these)
            Map.entry("PLOT", "PLOT"),
            Map.entry("LAND", "LAND"));

    // ── LOAN TYPE NORMALIZATION ──────────────────────────────────────────────
    // Frontend sends full names (HOME_LOAN, LOAN_AGAINST_PROPERTY).
    // V19 DB stores short codes (HL, LAP, BL, PL).
    private static final Map<String, String> LOAN_TYPE_NORMALIZATION = Map.of(
            "HOME_LOAN", "HL",
            "LOAN_AGAINST_PROPERTY", "LAP",
            "BUSINESS_LOAN", "BL",
            "PERSONAL_LOAN", "PL",
            "CREDIT_CARD", "CC");

    // ── EMPLOYMENT TYPE NORMALIZATION ────────────────────────────────────────
    // Frontend sends SALARIED, SELF_EMPLOYED, PROFESSIONAL.
    // V19 DB stores Salaried, SEP/SENP.
    private static final Map<String, String> EMPLOYMENT_TYPE_NORMALIZATION = Map.of(
            "SELF_EMPLOYED", "SEP/SENP",
            "PROFESSIONAL", "SEP/SENP",
            "SALARIED", "Salaried");

    /**
     * Resolve a frontend property sub-type to its bank-policy category.
     * Falls back to the input itself if no mapping exists (forward-compatible).
     */
    private static String resolvePropertyCategory(String propertyType) {
        if (propertyType == null)
            return "RESIDENTIAL";
        return PROPERTY_SUBTYPE_TO_CATEGORY.getOrDefault(propertyType.toUpperCase(), propertyType.toUpperCase());
    }

    /**
     * Resolve a frontend loanType to its DB short code.
     * Falls back to the input itself if no mapping exists (forward-compatible).
     */
    private static String normalizeLoanType(String loanType) {
        if (loanType == null)
            return "HL";
        return LOAN_TYPE_NORMALIZATION.getOrDefault(loanType.toUpperCase(), loanType.toUpperCase());
    }

    /**
     * Resolve a frontend employmentType to its DB value.
     * Falls back to the input itself if no mapping exists (forward-compatible).
     */
    private static String normalizeEmploymentType(String empType) {
        if (empType == null)
            return null;
        return EMPLOYMENT_TYPE_NORMALIZATION.getOrDefault(empType.toUpperCase(), empType);
    }

    private static boolean matchEmploymentType(String rowEmpType, String applicantEmpType) {
        if (rowEmpType == null || rowEmpType.isBlank()) {
            return true;
        }
        if (applicantEmpType == null) {
            return false;
        }
        if (rowEmpType.equalsIgnoreCase("SALARIED_SEP")) {
            return applicantEmpType.equalsIgnoreCase("Salaried") || applicantEmpType.equalsIgnoreCase("SEP/SENP");
        } else if (rowEmpType.equalsIgnoreCase("SEP_SENP")
                || rowEmpType.equalsIgnoreCase("SENP")
                || rowEmpType.equalsIgnoreCase("SEP")
                || rowEmpType.equalsIgnoreCase("SENP (Industry Margin)")) {
            return applicantEmpType.equalsIgnoreCase("SEP/SENP");
        } else {
            return rowEmpType.equalsIgnoreCase(applicantEmpType);
        }
    }

    public List<EligibilityResult> evaluate(EligibilityRequest request) {

        // ── STEP 1: General pre-flight gate (cheapest check, runs first) ──────
        var preflightRequest = new PreflightRequest(request);

        var preflightResult = generalPolicyPreflightService.evaluate(preflightRequest);

        if (!preflightResult.passed()) {
            // FIX BUG-A: record accessor is violations(), not getViolations()
            return List.of(buildPreflightRejectedResult(
                    request,
                    preflightResult.violations(),
                    "Pre-flight gate failed"));
        }

        // ── STEP 1.5: GEO-FENCE — Indore-only operations ─────────────────────
        // PRYME currently operates exclusively in Indore.
        // Valid Indore pincodes: 452xxx (Indore city) and 453xxx (Indore district).
        // If pinCode is provided but not from Indore, reject immediately.
        if (request.pinCode() != null && !request.pinCode().isBlank()) {
            String pin = request.pinCode().trim();
            boolean isIndore = pin.length() == 6
                    && (pin.startsWith("452") || pin.startsWith("453"));
            if (!isIndore) {
                log.info("🚫 GEO-FENCE: pinCode={} is outside Indore. Rejecting.", pin);
                return List.of(buildPreflightRejectedResult(
                        request,
                        List.of(String.format("Service area restricted: PIN %s is outside Indore (452xxx/453xxx)",
                                pin)),
                        "PRYME currently operates only in Indore"));
            }
        }

        // ── STEP 2: Load candidate products by CIBIL band ────────────────────
        final String normalizedLoanType = normalizeLoanType(request.loanType());
        log.info("🔍 STEP 2: loanType='{}' → normalized='{}', cibil={}",
                request.loanType(), normalizedLoanType, request.cibilScore());

        var allCibilMatches = loanProductRepository
                .findByMinCibilLessThanEqualAndMaxCibilGreaterThanEqual(
                        request.cibilScore(), request.cibilScore());
        log.info("   CIBIL band returned {} products (before loanType/lender filter)", allCibilMatches.size());

        var candidates = allCibilMatches.stream()
                .filter(p -> (request.lenderId() == null || p.getLenderId().equals(request.lenderId()))
                        && p.getLoanType().equalsIgnoreCase(normalizedLoanType)
                        && p.isActive())
                // Filter out products designed for a different employment profile
                .filter(p -> isProductAllowedForEmploymentType(p.getProductCode(), p.getLenderName(),
                        request.employmentType()))
                // ── LOAN AMOUNT RANGE GATE ──────────────────────────────────
                // Products define min_loan_amount / max_loan_amount boundaries.
                // A ₹25L request must NOT surface a product with min ₹35L.
                // This was the root cause of "same products always appear".
                .filter(p -> {
                    BigDecimal reqAmt = request.loanAmount();
                    if (reqAmt == null || reqAmt.compareTo(BigDecimal.ZERO) <= 0)
                        return true; // No amount → don't filter
                    boolean aboveMin = p.getMinLoanAmount() == null || reqAmt.compareTo(p.getMinLoanAmount()) >= 0;
                    boolean belowMax = p.getMaxLoanAmount() == null || reqAmt.compareTo(p.getMaxLoanAmount()) <= 0;
                    if (!aboveMin || !belowMax) {
                        log.info("   ⛔ LOAN_AMOUNT_RANGE: product={} excluded — requested={} not in [{}, {}]",
                                p.getProductCode(), reqAmt, p.getMinLoanAmount(), p.getMaxLoanAmount());
                    }
                    return aboveMin && belowMax;
                })
                .toList();
        log.info("   After loanType='{}' + active + loanAmount filter: {} candidates", normalizedLoanType,
                candidates.size());

        if (candidates.isEmpty()) {
            log.warn(
                    "❌ STEP 2 FAILED: No candidates after filter. loanType='{}' → normalized='{}', cibil={}, lenderId={}",
                    request.loanType(), normalizedLoanType, request.cibilScore(), request.lenderId());
            return List.of(buildPreflightRejectedResult(
                    request,
                    List.of(String.format("No matching products for CIBIL %d and loanType %s",
                            request.cibilScore(), normalizedLoanType)),
                    "No active loan products found for this lender and loan type"));
        }

        // ── STEP 3: Per-product evaluation ───────────────────────────────────
        var results = new ArrayList<EligibilityResult>();

        for (var product : candidates) {
            results.add(evaluateProductCascaded(product, request));
        }

        // ── STEP 4: Sort — eligible first, then best ROI ─────────────────────
        results.sort(Comparator.comparing(EligibilityResult::isEligible).reversed()
                .thenComparing(EligibilityResult::roi));

        log.info("Eligibility evaluation complete: totalCandidates={} eligible={} topProduct={}",
                candidates.size(),
                results.stream().filter(EligibilityResult::isEligible).count(),
                results.isEmpty() ? "None" : results.get(0).productCode());

        return results;
    }

    public List<PolicyFieldDefinition> getEligibilityConditionFields() {
        return policyFieldDefinitionRepository.findByEntityTypeAndIsActive(
                PolicyFieldDefinition.PolicyEntityType.ELIGIBILITY_CONDITION, true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-product evaluation — extracted to keep evaluate() readable
    // ─────────────────────────────────────────────────────────────────────────
    private EligibilityResult evaluateProduct(LoanProduct product, EligibilityRequest request) {
        long startTime = System.nanoTime();

        // a. Load eligibility conditions for this product
        var allConditions = eligibilityConditionRepository.findByProductId(product.getId());

        // ── SURROGATE SCOPE FILTER ───────────────────────────────────────────
        // Conditions with a non-null `surrogate` field only apply to applicants
        // using that specific income program. Conditions with null surrogate
        // apply universally. This makes the surrogate field a scope selector,
        // not a fail gate — an NIP condition won't block a BANKING applicant.
        String applicantProgram = (request.incomeComputationInput() != null
                && request.incomeComputationInput().programName() != null)
                        ? request.incomeComputationInput().programName().toUpperCase()
                        : null;

        // ── EMPLOYMENT TYPE FILTER ───────────────────────────────────────────
        // Each condition represents an eligibility lane for a specific employment
        // type (SALARIED, SELF_EMPLOYED, PROFESSIONAL). A SALARIED applicant
        // should only be evaluated against SALARIED conditions. Conditions with
        // null employment_type are universal.
        //
        // NORMALIZATION: Frontend sends SELF_EMPLOYED/PROFESSIONAL → DB stores
        // SEP/SENP.
        final String normalizedEmpType = normalizeEmploymentType(
                request.employmentType() != null ? request.employmentType() : null);
        log.info("   STEP 3: empType='{}' → normalized='{}', surrogate='{}', product={}",
                request.employmentType(), normalizedEmpType, applicantProgram, product.getProductCode());

        // d. Resolve surrogate income (monthly, BigDecimal precision)
        final BigDecimal computedIncome = surrogateIncomeResolver.resolve(request.incomeComputationInput());

        // Uses the best available income figure: computed surrogate income
        // first, then declared grossMonthlyIncome if present.
        BigDecimal incomeForFloorCheck = computedIncome;
        if (incomeForFloorCheck == null || incomeForFloorCheck.compareTo(BigDecimal.ZERO) <= 0) {
            if (request.grossMonthlyIncome() != null && request.grossMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
                incomeForFloorCheck = request.grossMonthlyIncome();
            } else {
                incomeForFloorCheck = request.monthlyIncome();
            }
        }
        final BigDecimal effectiveIncome = incomeForFloorCheck != null ? incomeForFloorCheck : BigDecimal.ZERO;

        var conditions = allConditions.stream()
                .filter(c -> c.getSurrogate() == null
                        || c.getSurrogate().isBlank()
                        || (applicantProgram != null
                                && c.getSurrogate().equalsIgnoreCase(applicantProgram)))
                .filter(c -> matchEmploymentType(c.getEmploymentType(), normalizedEmpType))
                .sorted((c1, c2) -> {
                    BigDecimal min1 = c1.getMinIncome() != null ? c1.getMinIncome() : BigDecimal.ZERO;
                    BigDecimal min2 = c2.getMinIncome() != null ? c2.getMinIncome() : BigDecimal.ZERO;
                    return min2.compareTo(min1); // Descending order to match highest bracket first
                })
                .toList();

        List<RuleEvaluation> rulesEvaluated = new ArrayList<>();
        List<FormulaTrace> formulasEvaluated = new ArrayList<>();

        // Add income computation trace
        formulasEvaluated.add(traceIncome(request.incomeComputationInput(), effectiveIncome));

        // If no conditions match the applicant's employment type + surrogate,
        // this product doesn't serve this applicant profile at all.
        if (conditions.isEmpty()) {
            log.warn(
                    "⏭️ Product {} has no conditions for empType='{}' (normalized='{}'), surrogate='{}'. Skipping. DB conditions had empTypes: {}",
                    product.getProductCode(), request.employmentType(), normalizedEmpType, applicantProgram,
                    allConditions.stream().map(EligibilityCondition::getEmploymentType).distinct().toList());
            
            List<String> reasons = List.of(String.format("No eligibility lane for empType=%s (normalized=%s), surrogate=%s",
                    request.employmentType(), normalizedEmpType, applicantProgram));
            
            rulesEvaluated.add(new RuleEvaluation(
                    "CONDITION_LANE",
                    DecisionStatus.FAIL,
                    "Conditions exist matching empType & surrogate",
                    false,
                    "No matching condition lane for applicant profile"
            ));

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            ProgramType progType = parseProgramType(applicantProgram);
            DecisionStep step = new DecisionStep(
                    progType,
                    DecisionStatus.FAIL,
                    duration,
                    null, null, null,
                    effectiveIncome, null, null, null, null,
                    null, BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    progType,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return new EligibilityResult(
                    false, product.getProductCode(), product.getLenderName(), applicantProgram,
                    effectiveIncome, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    false, reasons, "No matching condition lane for applicant profile",
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    trace
            );
        }

        // ── BUILD APPLICANT PAYLOAD for SpEL rules ───────────────────────────
        Map<String, Object> applicantPayload = new HashMap<>();
        applicantPayload.put("cibilScore", request.cibilScore());
        applicantPayload.put("loanAmount", request.loanAmount());
        applicantPayload.put("propertyValue", request.propertyValue());
        applicantPayload.put("existingEmiTotal", request.existingEmiTotal());
        applicantPayload.put("requestedTenureMonths", request.requestedTenureMonths());
        applicantPayload.put("applicantAge", request.applicantAge());
        applicantPayload.put("cityTier", request.cityTier());
        applicantPayload.put("propertyType", request.propertyType());
        applicantPayload.put("businessAgeYears", request.businessAgeYears());
        applicantPayload.put("workExpYears", request.workExpYears());
        applicantPayload.put("employmentType", request.employmentType());

        // ── PROPERTY TYPE NORMALIZATION ───────────────────────────────────
        // Frontend sends sub-types (FLAT, HOME, SHOP, etc.).
        // DB stores categories (RESIDENTIAL, COMMERCIAL, INDUSTRIAL).
        // Resolve the sub-type to its parent category for allow-list matching.
        final String resolvedPropertyCategory = resolvePropertyCategory(request.propertyType());
        final String rawPropertySubType = request.propertyType() != null ? request.propertyType().toUpperCase()
                : "RESIDENTIAL";
        applicantPayload.put("propertyCategory", resolvedPropertyCategory);

        // b. Condition checks: age, business vintage, work experience, property,
        // city, CIBIL floor, ITR requirement + SpEL rules
        //
        // 🧠 CORRECT SEMANTICS: Each condition is an independent eligibility LANE.
        // The product is eligible if AT LEAST ONE condition passes all checks.
        // Example: HDFC Home Loan has 4 conditions:
        // - Salaried NIP (workExp >= 1, FOIR 75%, LTV 65%)
        // - Self-Employed NIP (bizAge >= 3, FOIR 95%, LTV 45%)
        // - Self-Employed BANKING (bizAge >= 3, FOIR 55%, LTV 85%)
        // - Self-Employed GST (bizAge >= 2, FOIR 75%, LTV 65%)
        // A SALARIED applicant need only pass the first one.
        EligibilityCondition matchedCondition = null;
        List<String> allRejectionReasons = new ArrayList<>();

        for (var c : conditions) {
            List<String> reasonsForThisCondition = new ArrayList<>();
            List<RuleEvaluation> laneRules = new ArrayList<>();

            // ── Standard numeric/range checks ────────
            if (c.getMinAge() != null) {
                boolean pass = request.applicantAge() >= c.getMinAge();
                laneRules.add(new RuleEvaluation(
                        "MIN_AGE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMinAge(),
                        request.applicantAge(),
                        pass ? "Age meets min age limit" : String.format("applicantAge=%d < minAge=%d", request.applicantAge(), c.getMinAge())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("AGE_TOO_LOW: applicantAge=%d < minAge=%d (condition=%d, surrogate=%s)",
                            request.applicantAge(), c.getMinAge(), c.getId(), c.getSurrogate()));
                }
            }
            double ageAtMaturity = request.applicantAge() + (request.requestedTenureMonths() / 12.0);
            if (c.getMaxAge() != null) {
                boolean pass = ageAtMaturity <= c.getMaxAge();
                laneRules.add(new RuleEvaluation(
                        "MAX_AGE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMaxAge(),
                        ageAtMaturity,
                        pass ? "Age at maturity meets max age limit" : String.format("ageAtMaturity=%.2f > maxAge=%d", ageAtMaturity, c.getMaxAge())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("AGE_TOO_HIGH: ageAtMaturity=%.2f > maxAge=%d (condition=%d, surrogate=%s)",
                            ageAtMaturity, c.getMaxAge(), c.getId(), c.getSurrogate()));
                }
            }
            if (c.getBusinessAgeYears() != null) {
                boolean pass = request.businessAgeYears() >= c.getBusinessAgeYears();
                laneRules.add(new RuleEvaluation(
                        "BUSINESS_AGE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getBusinessAgeYears(),
                        request.businessAgeYears(),
                        pass ? "Business age meets limit" : String.format("businessAge=%d < required=%d", request.businessAgeYears(), c.getBusinessAgeYears())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("BIZ_VINTAGE_LOW: businessAge=%d < required=%d (condition=%d, surrogate=%s)",
                            request.businessAgeYears(), c.getBusinessAgeYears(), c.getId(), c.getSurrogate()));
                }
            }
            if (c.getWorkExpYears() != null) {
                boolean pass = request.workExpYears() >= c.getWorkExpYears();
                laneRules.add(new RuleEvaluation(
                        "WORK_EXP",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getWorkExpYears(),
                        request.workExpYears(),
                        pass ? "Work experience meets limit" : String.format("workExp=%d < required=%d", request.workExpYears(), c.getWorkExpYears())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("WORK_EXP_LOW: workExp=%d < required=%d (condition=%d, surrogate=%s)",
                            request.workExpYears(), c.getWorkExpYears(), c.getId(), c.getSurrogate()));
                }
            }

            // ── PROPERTY TYPE ALLOW-LIST CHECK ────────
            if (c.getPropertyType() != null) {
                String allowedRaw = c.getPropertyType().toUpperCase();
                boolean propertyAllowed = Arrays.stream(allowedRaw.split("[,;]"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .anyMatch(allowed -> allowed.equalsIgnoreCase(resolvedPropertyCategory)
                                || allowed.equalsIgnoreCase(rawPropertySubType));
                laneRules.add(new RuleEvaluation(
                        "PROPERTY_TYPE",
                        propertyAllowed ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getPropertyType(),
                        rawPropertySubType,
                        propertyAllowed ? "Property type allowed" : String.format("subType=%s not allowed by list: %s", rawPropertySubType, c.getPropertyType())
                ));
                if (!propertyAllowed) {
                    reasonsForThisCondition.add(String.format(
                            "PROPERTY_NOT_ALLOWED: subType=%s, resolved=%s, allowList='%s' (condition=%d, surrogate=%s)",
                            rawPropertySubType, resolvedPropertyCategory, c.getPropertyType(), c.getId(),
                            c.getSurrogate()));
                }
            }

            if (c.getCityTier() != null) {
                boolean pass = c.getCityTier().equalsIgnoreCase(request.cityTier());
                laneRules.add(new RuleEvaluation(
                        "CITY_TIER",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getCityTier(),
                        request.cityTier(),
                        pass ? "City tier matches" : String.format("requested=%s, required=%s", request.cityTier(), c.getCityTier())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("CITY_TIER_MISMATCH: requested=%s, required=%s (condition=%d)",
                            request.cityTier(), c.getCityTier(), c.getId()));
                }
            }
            if (c.getCibilMin() != null) {
                boolean pass = request.cibilScore() >= c.getCibilMin();
                laneRules.add(new RuleEvaluation(
                        "MIN_CIBIL",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getCibilMin(),
                        request.cibilScore(),
                        pass ? "CIBIL score meets minimum limit" : String.format("score=%d < required=%d", request.cibilScore(), c.getCibilMin())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("CIBIL_TOO_LOW: score=%d < conditionMin=%d (condition=%d)",
                            request.cibilScore(), c.getCibilMin(), c.getId()));
                }
            }
            if (c.getItrRequiredYears() != null) {
                boolean pass = request.itrYearsAvailable() == null || request.itrYearsAvailable() >= c.getItrRequiredYears();
                laneRules.add(new RuleEvaluation(
                        "ITR_YEARS",
                        request.itrYearsAvailable() == null ? DecisionStatus.SKIPPED : (pass ? DecisionStatus.PASS : DecisionStatus.FAIL),
                        c.getItrRequiredYears(),
                        request.itrYearsAvailable(),
                        request.itrYearsAvailable() == null ? "ITR history not provided, skipping check" : (pass ? "ITR history sufficient" : String.format("available=%d < required=%d", request.itrYearsAvailable(), c.getItrRequiredYears()))
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("ITR_YEARS_LOW: available=%d < required=%d (condition=%d)",
                            request.itrYearsAvailable() != null ? request.itrYearsAvailable() : 0, c.getItrRequiredYears(), c.getId()));
                }
            }

            if (c.getMinIncome() != null) {
                BigDecimal minIncLimit = c.getMinIncome();
                boolean floorMatched;
                java.math.BigInteger intVal = minIncLimit.setScale(0, RoundingMode.DOWN).toBigInteger();
                String intStr = intVal.toString();
                if (intStr.endsWith("1") && minIncLimit.compareTo(BigDecimal.ONE) > 0) {
                    BigDecimal boundary = new BigDecimal(intVal.subtract(java.math.BigInteger.ONE));
                    floorMatched = effectiveIncome.compareTo(boundary) > 0;
                } else {
                    floorMatched = effectiveIncome.compareTo(minIncLimit) >= 0;
                }
                laneRules.add(new RuleEvaluation(
                        "INCOME_FLOOR",
                        floorMatched ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        minIncLimit,
                        effectiveIncome,
                        floorMatched ? "Income meets floor" : String.format("income=%s < required=%s", effectiveIncome, minIncLimit)
                ));
                if (!floorMatched) {
                    reasonsForThisCondition.add(String.format("INCOME_FLOOR_FAILED: income=%s < required=%s (condition=%d, surrogate=%s)",
                            effectiveIncome, minIncLimit, c.getId(), c.getSurrogate()));
                }
            }
            if (c.getMinLoanAmount() != null) {
                boolean pass = request.loanAmount() != null && request.loanAmount().compareTo(c.getMinLoanAmount()) >= 0;
                laneRules.add(new RuleEvaluation(
                        "MIN_LOAN_AMOUNT",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMinLoanAmount(),
                        request.loanAmount(),
                        pass ? "Loan amount meets min limit" : String.format("amount=%s < min=%s", request.loanAmount(), c.getMinLoanAmount())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("LOAN_AMOUNT_TOO_LOW: amount=%s < min=%s (condition=%d)",
                            request.loanAmount(), c.getMinLoanAmount(), c.getId()));
                }
            }
            if (c.getMaxLoanAmount() != null) {
                boolean pass = request.loanAmount() != null && request.loanAmount().compareTo(c.getMaxLoanAmount()) <= 0;
                laneRules.add(new RuleEvaluation(
                        "MAX_LOAN_AMOUNT",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMaxLoanAmount(),
                        request.loanAmount(),
                        pass ? "Loan amount meets max limit" : String.format("amount=%s > max=%s", request.loanAmount(), c.getMaxLoanAmount())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("LOAN_AMOUNT_TOO_HIGH: amount=%s > max=%s (condition=%d)",
                            request.loanAmount(), c.getMaxLoanAmount(), c.getId()));
                }
            }
            if (c.getMinTenure() != null) {
                boolean pass = request.requestedTenureMonths() >= c.getMinTenure();
                laneRules.add(new RuleEvaluation(
                        "MIN_TENURE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMinTenure(),
                        request.requestedTenureMonths(),
                        pass ? "Tenure meets min limit" : String.format("tenure=%d < min=%d", request.requestedTenureMonths(), c.getMinTenure())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("TENURE_TOO_LOW: tenure=%d < min=%d (condition=%d)",
                            request.requestedTenureMonths(), c.getMinTenure(), c.getId()));
                }
            }
            if (c.getMaxTenure() != null) {
                boolean pass = request.requestedTenureMonths() <= c.getMaxTenure();
                laneRules.add(new RuleEvaluation(
                        "MAX_TENURE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMaxTenure(),
                        request.requestedTenureMonths(),
                        pass ? "Tenure meets max limit" : String.format("tenure=%d > max=%d", request.requestedTenureMonths(), c.getMaxTenure())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("TENURE_TOO_HIGH: tenure=%d > max=%d (condition=%d)",
                            request.requestedTenureMonths(), c.getMaxTenure(), c.getId()));
                }
            }

            // ── SpEL / deny-list / memo rules ────────
            if (c.getProfileRestrictions() != null && !c.getProfileRestrictions().isEmpty()) {
                boolean pass = smartEvaluate(c.getProfileRestrictions(), applicantPayload, "employmentType");
                laneRules.add(new RuleEvaluation(
                        "PROFILE_RESTRICTIONS",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getProfileRestrictions(),
                        request.employmentType(),
                        pass ? "Profile allowed" : "Profile restricted"
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("PROFILE_RESTRICTED: empType=%s hit deny-list (condition=%d, surrogate=%s)",
                            request.employmentType(), c.getId(), c.getSurrogate()));
                }
            }
            if (c.getNegativeProperty() != null && !c.getNegativeProperty().isEmpty()) {
                boolean pass = smartEvaluatePropertyDenyList(c.getNegativeProperty(), rawPropertySubType, resolvedPropertyCategory);
                laneRules.add(new RuleEvaluation(
                        "NEGATIVE_PROPERTY",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getNegativeProperty(),
                        rawPropertySubType,
                        pass ? "Property allowed" : "Property restricted"
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("NEGATIVE_PROPERTY: subType=%s, category=%s denied by '%s' (condition=%d)",
                            rawPropertySubType, resolvedPropertyCategory, c.getNegativeProperty(), c.getId()));
                }
            }
            if (c.getConditions() != null && !c.getConditions().isEmpty()) {
                boolean pass = smartEvaluate(c.getConditions(), applicantPayload, null);
                laneRules.add(new RuleEvaluation(
                        "SPEL_CONDITIONS",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getConditions(),
                        "payload",
                        pass ? "Conditions pass" : "Conditions fail"
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("CONDITION_RULE_FAILED: rule='%s' (condition=%d)",
                            c.getConditions(), c.getId()));
                }
            }
            if (c.getDeviationFormulae() != null && !c.getDeviationFormulae().isEmpty()) {
                boolean pass = smartEvaluate(c.getDeviationFormulae(), applicantPayload, null);
                laneRules.add(new RuleEvaluation(
                        "DEVIATION_FORMULAE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getDeviationFormulae(),
                        "payload",
                        pass ? "Deviation pass" : "Deviation fail"
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("DEVIATION_RULE_FAILED: rule='%s' (condition=%d)",
                            c.getDeviationFormulae(), c.getId()));
                }
            }

            rulesEvaluated.addAll(laneRules);

            // ── If this condition passed ALL checks → product is eligible via this lane
            if (reasonsForThisCondition.isEmpty()) {
                matchedCondition = c;
                log.info("✅ Product {} PASSED via condition lane [id={}, empType={}, surrogate={}]",
                        product.getProductCode(), c.getId(), c.getEmploymentType(), c.getSurrogate());
                break; // One pass is enough — short-circuit
            }

            // Otherwise, collect reasons for diagnostic reporting
            allRejectionReasons.addAll(reasonsForThisCondition);
        }

        // If no condition passed, the product is ineligible
        if (matchedCondition == null) {
            log.warn(
                    "❌ Product {} REJECTED for applicant [cibil={}, empType={}, propType={}, age={}]. All {} condition lanes failed. Reasons: {}",
                    product.getProductCode(), request.cibilScore(), request.employmentType(),
                    request.propertyType(), request.applicantAge(), conditions.size(), allRejectionReasons);

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            ProgramType progType = parseProgramType(applicantProgram);
            DecisionStep step = new DecisionStep(
                    progType,
                    DecisionStatus.FAIL,
                    duration,
                    null, null, null,
                    effectiveIncome, null, null, null, null,
                    null, BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    progType,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return EligibilityResult.ineligible(
                    product.getProductCode(),
                    product.getLenderName(),
                    allRejectionReasons,
                    "Applicant profile does not satisfy any eligibility lane"
            );
        }

        // ── Resolve effective LTV from matched condition (static field)
        final BigDecimal effectiveLtv = matchedCondition.getLtvAllowed() != null
                ? matchedCondition.getLtvAllowed()
                : (product.getLtv() != null ? product.getLtv() : BigDecimal.ZERO);

        // c. Resolve effective FOIR from matched condition (static field)
        BigDecimal foirMaxVal = matchedCondition.getFoirMax();
        if (foirMaxVal == null) {
            if ("ICICI Bank".equalsIgnoreCase(product.getLenderName())
                    && "NIP".equalsIgnoreCase(matchedCondition.getSurrogate())
                    && ("SENP".equalsIgnoreCase(matchedCondition.getEmploymentType())
                            || "SEP_SENP".equalsIgnoreCase(matchedCondition.getEmploymentType())
                            || "SEP".equalsIgnoreCase(matchedCondition.getEmploymentType()))) {
                foirMaxVal = new BigDecimal("1.40").subtract(effectiveLtv);
                log.info("Dynamic ICICI FOIR resolved: 1.40 - LTV ({}) = {}", effectiveLtv, foirMaxVal);
            } else {
                foirMaxVal = (product.getMaxEmiNmiRatio() != null ? product.getMaxEmiNmiRatio() : DEFAULT_FOIR);
            }
        }
        final BigDecimal effectiveFoir = foirMaxVal;

        // d. Resolve surrogate income (monthly, BigDecimal precision) - already computed before loop

        // ── ROI: Resolve dynamically via FinancialComputationEngine ────────────
        ApplicantProfile applicantProfile = new ApplicantProfile(request.cibilScore(), normalizedEmpType,
                effectiveIncome);
        final BigDecimal effectiveRoi = financialComputationEngine.resolveRoi(product, applicantProfile,
                request.loanAmount());

        log.debug("ROI resolved: product={} computedRoi={}",
                product.getProductCode(), effectiveRoi);

        // e. Calculate proposed EMI using closed-form PMT formula
        var proposedEmi = calculateProposedEmiWithRate(request.loanAmount(), effectiveRoi,
                request.requestedTenureMonths());

        formulasEvaluated.add(new FormulaTrace(
                "EMI",
                "principal * [r(1+r)^n] / [(1+r)^n - 1]",
                Map.of(
                        "principal", request.loanAmount(),
                        "roi", effectiveRoi,
                        "tenureMonths", request.requestedTenureMonths()
                ),
                proposedEmi
        ));

        // f. FOIR check
        boolean foirPass = checkFoir(request.existingEmiTotal(), proposedEmi, effectiveIncome, effectiveFoir);
        BigDecimal totalEmi = safe(request.existingEmiTotal()).add(safe(proposedEmi));
        BigDecimal actualFoir = (effectiveIncome.compareTo(BigDecimal.ZERO) > 0)
                ? totalEmi.divide(effectiveIncome, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        rulesEvaluated.add(new RuleEvaluation(
                "FOIR_LIMIT",
                foirPass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                "<=" + effectiveFoir.setScale(4, RoundingMode.HALF_UP),
                actualFoir.setScale(4, RoundingMode.HALF_UP),
                foirPass ? "FOIR within limit" : "FOIR exceeded limit"
        ));

        formulasEvaluated.add(new FormulaTrace(
                "FOIR",
                "(existingEmiTotal + proposedEmi) / computedIncome",
                Map.of(
                        "existingEmiTotal", safe(request.existingEmiTotal()),
                        "proposedEmi", safe(proposedEmi),
                        "computedIncome", effectiveIncome
                ),
                actualFoir
        ));

        if (!foirPass) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            ProgramType progType = parseProgramType(applicantProgram);
            DecisionStep step = new DecisionStep(
                    progType,
                    DecisionStatus.FAIL,
                    duration,
                    matchedCondition.getId(), matchedCondition.getEmploymentType(), matchedCondition.getSurrogate(),
                    effectiveIncome, effectiveFoir, effectiveRoi, proposedEmi, BigDecimal.ZERO,
                    null, BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    progType,
                    BigDecimal.ZERO, effectiveRoi, effectiveLtv,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return new EligibilityResult(
                    false, product.getProductCode(), product.getLenderName(), applicantProgram,
                    effectiveIncome, effectiveFoir, proposedEmi,
                    BigDecimal.ZERO, effectiveRoi, request.requestedTenureMonths(), effectiveLtv,
                    false, List.of(String.format("FOIR exceeded: effective limit is %.0f%%",
                            effectiveFoir.multiply(BigDecimal.valueOf(100)))),
                    "Total EMI obligations exceed the program FOIR limit",
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    trace
            );
        }

        // g. Maximum eligible loan amount (income × FOIR − existing EMI)
        var maxEligibleAmount = calculateMaxEligibleAmount(
                effectiveIncome, request.existingEmiTotal(), effectiveFoir, effectiveRoi, request.requestedTenureMonths());

        formulasEvaluated.add(new FormulaTrace(
                "MAX_ELIGIBLE",
                "maxAllowedEmi / factor",
                Map.of(
                        "computedIncome", effectiveIncome,
                        "effectiveFoir", effectiveFoir,
                        "existingEmiTotal", safe(request.existingEmiTotal()),
                        "roi", effectiveRoi,
                        "tenureMonths", request.requestedTenureMonths()
                ),
                maxEligibleAmount
        ));

        // h. LTV check — USES effectiveLtv (condition-level override)
        BigDecimal maxLtvAmount = request.propertyValue().multiply(effectiveLtv, MathContext.DECIMAL128);
        boolean ltvDeviated = request.loanAmount().compareTo(maxLtvAmount) > 0;

        rulesEvaluated.add(new RuleEvaluation(
                "LTV_LIMIT",
                !ltvDeviated ? DecisionStatus.PASS : DecisionStatus.FAIL,
                "<=" + maxLtvAmount.setScale(2, RoundingMode.HALF_UP),
                request.loanAmount(),
                !ltvDeviated ? "LTV within limit" : "LTV exceeded limit"
        ));

        formulasEvaluated.add(new FormulaTrace(
                "LTV_CAP",
                "propertyValue * effectiveLtv",
                Map.of(
                        "propertyValue", request.propertyValue(),
                        "effectiveLtv", effectiveLtv
                ),
                maxLtvAmount
        ));

        if (ltvDeviated) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            ProgramType progType = parseProgramType(applicantProgram);
            DecisionStep step = new DecisionStep(
                    progType,
                    DecisionStatus.FAIL,
                    duration,
                    matchedCondition.getId(), matchedCondition.getEmploymentType(), matchedCondition.getSurrogate(),
                    effectiveIncome, effectiveFoir, effectiveRoi, proposedEmi, maxEligibleAmount,
                    null, BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    progType,
                    BigDecimal.ZERO, effectiveRoi, effectiveLtv,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return new EligibilityResult(
                    false, product.getProductCode(), product.getLenderName(), applicantProgram,
                    effectiveIncome, effectiveFoir, proposedEmi,
                    BigDecimal.ZERO, effectiveRoi, request.requestedTenureMonths(), effectiveLtv,
                    true, List.of(String.format("LTV_EXCEEDED: requested %.2f, allowed %.2f (LTV limit: %.0f%%)",
                            request.loanAmount(), maxLtvAmount, effectiveLtv.multiply(BigDecimal.valueOf(100)))),
                    "Requested loan amount exceeds the maximum allowed LTV for this product",
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    trace
            );
        }

        var finalLoanAmount = request.loanAmount()
                .min(maxLtvAmount)
                .min(maxEligibleAmount);

        // ── Processing Fee: dynamic resolution ──────────────────────────────
        final BigDecimal processingFee = financialComputationEngine.resolveProcessingFee(
                product, finalLoanAmount, normalizedEmpType);

        // ── Login Fee: dynamic resolution ───────────────────────────────────
        final BigDecimal loginFee = financialComputationEngine.resolveLoginFee(
                product, finalLoanAmount, normalizedEmpType);

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        ProgramType progType = parseProgramType(applicantProgram);
        DecisionStep step = new DecisionStep(
                progType,
                DecisionStatus.PASS,
                duration,
                matchedCondition.getId(), matchedCondition.getEmploymentType(), matchedCondition.getSurrogate(),
                effectiveIncome, effectiveFoir, effectiveRoi, proposedEmi, finalLoanAmount,
                new LtvDetail(request.propertyValue(), effectiveLtv, "propertyValue * effectiveLtv", maxLtvAmount, "DB:condition_id=" + matchedCondition.getId()),
                processingFee, loginFee,
                rulesEvaluated, formulasEvaluated
        );
        DecisionSummary summary = new DecisionSummary(
                DecisionStatus.PASS,
                progType,
                finalLoanAmount, effectiveRoi, effectiveLtv,
                1, 1, 0
        );
        DecisionTrace trace = new DecisionTrace(
                UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                duration, buildRequestSnapshot(request), List.of(step), summary
        );

        return new EligibilityResult(
                true, product.getProductCode(), product.getLenderName(), applicantProgram,
                effectiveIncome, effectiveFoir, proposedEmi,
                finalLoanAmount, effectiveRoi, request.requestedTenureMonths(), effectiveLtv,
                false, List.of(), "Eligible",
                processingFee, loginFee,
                product.getAdminFee(), product.getInsuranceCharges(), product.getLegalTechnicalCharges(),
                product.getOtherExpense(), product.getStampDuties(), product.getPrepaymentCharges(),
                product.getForeclosureCharges(),
                matchedCondition.getVintage(), matchedCondition.getNegativeProperty(), matchedCondition.getNegativeEmployerType(),
                matchedCondition.getNegativeSalaryMode(), matchedCondition.getMarginByOccupation(), matchedCondition.getDeviationFormulae(),
                matchedCondition.getConditions(), matchedCondition.getEmiNotObligated(), matchedCondition.getBankStatementRequirement(),
                matchedCondition.getSalarySlipRequirement(), matchedCondition.getGstReturnRequirement(), matchedCondition.getProvidentFundMandatory(),
                matchedCondition.getItrRequiredYears(), matchedCondition.getProfileRestrictions(),
                trace
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PMT formula: EMI = P × [r(1+r)^n] / [(1+r)^n − 1]
    // Closed-form O(1) — no amortisation loop.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Original method signature preserved for backward compatibility.
     * Delegates to the rate-parameterised version using static product ROI.
     */
    private BigDecimal calculateProposedEmi(LoanProduct product, EligibilityRequest request) {
        return calculateProposedEmiWithRate(
                request.loanAmount(), product.getRoi(), request.requestedTenureMonths());
    }

    /**
     * Rate-parameterised EMI calculator — accepts the EFFECTIVE annual rate
     * (which may come from dynamic SpEL resolution instead of static product.roi).
     */
    private BigDecimal calculateProposedEmiWithRate(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        int effectiveTenure = tenureMonths > 0 ? tenureMonths : 12;

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // Zero-rate edge case: equal instalments
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(effectiveTenure), 2, RoundingMode.HALF_UP);
        }

        MathContext mc = MathContext.DECIMAL128;
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), mc);
        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate, mc).pow(effectiveTenure, mc);
        BigDecimal numerator = monthlyRate.multiply(onePlusRToN, mc);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE, mc);

        return principal.multiply(numerator.divide(denominator, mc), mc)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX BUG-G: guard against zero/null computedIncome → ArithmeticException
    // ─────────────────────────────────────────────────────────────────────────
    private boolean checkFoir(BigDecimal existingEmiTotal, BigDecimal proposedEmi,
            BigDecimal computedIncome, BigDecimal effectiveFoir) {
        if (computedIncome == null || computedIncome.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("checkFoir: computedIncome is zero or null — treating as FOIR failed");
            return false;
        }
        BigDecimal totalEmi = safe(existingEmiTotal).add(safe(proposedEmi));
        BigDecimal actualFoir = totalEmi.divide(computedIncome, 4, RoundingMode.HALF_UP);
        return actualFoir.compareTo(effectiveFoir) <= 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX BUG-H: .max(ZERO) prevents negative max eligible amount when
    // existing EMI already exceeds income × FOIR.
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal calculateMaxEligibleAmount(BigDecimal computedIncome,
                                                  BigDecimal existingEmiTotal,
                                                  BigDecimal effectiveFoir,
                                                  BigDecimal annualRate,
                                                  int tenureMonths) {
        BigDecimal maxAllowedEmi = computedIncome.multiply(effectiveFoir, MathContext.DECIMAL128)
                .subtract(safe(existingEmiTotal)).max(BigDecimal.ZERO);

        int effectiveTenure = tenureMonths > 0 ? tenureMonths : 12;
        if (maxAllowedEmi.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return maxAllowedEmi.multiply(BigDecimal.valueOf(effectiveTenure));
        }

        MathContext mc = MathContext.DECIMAL128;
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), mc);
        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate, mc).pow(effectiveTenure, mc);
        BigDecimal numerator = monthlyRate.multiply(onePlusRToN, mc);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE, mc);
        BigDecimal factor = numerator.divide(denominator, mc);

        return maxAllowedEmi.divide(factor, mc).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private Boolean evaluateProfileRule(Long ruleId, String expressionString, Map<String, Object> applicantPayload) {
        try {
            var expression = spelExpressionCacheService.getOrCompile(expressionString);
            return expression.getValue(simpleSandboxEvaluationContext, applicantPayload, Boolean.class);
        } catch (EvaluationException e) {
            log.error("SpEL evaluation failed for ruleId={} expression='{}'", ruleId, expressionString, e);
            throw new GeneralPolicyPreflightService.PolicyRuleValidationException(
                    "Rule evaluation error for expression: " + expressionString + " -> " + e.getMessage());
        }
    }

    /**
     * "200 IQ" Smart Evaluator: Handles SpEL expressions, implicit SpEL, text
     * deny-lists, and pure text memos.
     * Returns TRUE if the rule PASSES or is just a MEMO. Returns FALSE if the rule
     * FAILS.
     */
    private boolean smartEvaluate(String ruleText, Map<String, Object> payload, String targetField) {
        if (ruleText == null || ruleText.isBlank()) {
            return true; // No rule = Pass
        }

        String trimmed = ruleText.trim();

        // 1. Explicit SpEL rule
        if (trimmed.toUpperCase().startsWith("SPEL:")) {
            String spelContent = trimmed.substring(5).trim();
            try {
                var expr = spelExpressionCacheService.getOrCompile(spelContent);
                Boolean result = expr.getValue(simpleSandboxEvaluationContext, payload, Boolean.class);
                return Boolean.TRUE.equals(result);
            } catch (Exception e) {
                log.error("Explicit SpEL execution failed for rule '{}'. Rejecting. Error: {}", ruleText,
                        e.getMessage());
                return false; // Explicit SpEL failure blocks the loan for safety
            }
        }

        // 2. Implicit SpEL (contains #, ==, >, <)
        if (trimmed.contains("#") && (trimmed.contains("==") || trimmed.contains(">") || trimmed.contains("<")
                || trimmed.contains("!="))) {
            try {
                var expr = spelExpressionCacheService.getOrCompile(trimmed);
                Boolean result = expr.getValue(simpleSandboxEvaluationContext, payload, Boolean.class);
                return Boolean.TRUE.equals(result);
            } catch (Exception e) {
                log.trace("Implicit SpEL parsing failed for '{}', falling back to memo/list evaluation. Error: {}",
                        ruleText, e.getMessage());
                // Fall down to treat as list or memo
            }
        }

        // 3. Smart Deny-List (Comma or Semicolon separated list applied to targetField)
        if (targetField != null && (trimmed.contains(";") || trimmed.contains(",") || !trimmed.contains(" "))) {
            Object targetValueObj = payload.get(targetField);
            if (targetValueObj instanceof String targetValue) {
                boolean matchesDenyList = Arrays.stream(trimmed.split("[,;]"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .anyMatch(denyItem -> denyItem.equalsIgnoreCase(targetValue));

                if (matchesDenyList) {
                    return false; // Failed deny list
                }
                return true; // Pass
            }
        }

        // 4. Fallback (Internal Memo / English text)
        log.trace("Treating rule '{}' as an internal memo (not evaluated).", ruleText);
        return true;
    }

    /**
     * Specialized deny-list evaluator for negative property types.
     * Checks the deny-list against BOTH the raw sub-type (e.g., PLOT) and
     * the resolved category (e.g., RESIDENTIAL). If either matches, the
     * property is denied.
     *
     * @return true if the property PASSES (not in deny-list), false if denied
     */
    private boolean smartEvaluatePropertyDenyList(String denyListText, String rawSubType, String resolvedCategory) {
        if (denyListText == null || denyListText.isBlank()) {
            return true; // No deny-list = pass
        }

        String trimmed = denyListText.trim();

        // Parse as a deny-list (comma or semicolon separated, or single word)
        boolean matchesDenyList = Arrays.stream(trimmed.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(denyItem -> denyItem.equalsIgnoreCase(rawSubType) ||
                        denyItem.equalsIgnoreCase(resolvedCategory));

        if (matchesDenyList) {
            log.debug("Property '{}' (category: '{}') matches negative property deny-list: '{}'",
                    rawSubType, resolvedCategory, denyListText);
            return false; // Denied
        }
        return true; // Passed
    }

    private EligibilityResult evaluateProductCascaded(LoanProduct product, EligibilityRequest request) {
        long cascadeStartTime = System.nanoTime();
        List<DecisionStep> steps = new ArrayList<>();
        int stagesTried = 0;
        int stagesPassed = 0;
        int stagesFailed = 0;

        EligibilityRequest nipRequest = new EligibilityRequest(
                request.lenderId(),
                request.loanType(),
                request.cibilScore(),
                request.applicantAge(),
                request.employmentType(),
                request.propertyType(),
                request.cityTier(),
                request.loanAmount(),
                request.propertyValue(),
                request.requestedTenureMonths(),
                request.monthlyIncome(),
                request.existingEmiTotal(),
                request.businessAgeYears(),
                request.workExpYears(),
                getIncomeInputForProgram(request, "NIP"),
                request.idempotencyKey(),
                request.itrYearsAvailable(),
                request.grossMonthlyIncome(),
                request.pinCode(),
                request.propertyCategory(),
                request.businessPropertyCategory());

        EligibilityResult nipResult = null;
        long nipStart = System.nanoTime();
        stagesTried++;
        try {
            nipResult = evaluateProduct(product, nipRequest);
        } catch (Exception e) {
            log.warn("NIP evaluation failed for product={}: {}", product.getProductCode(), e.getMessage());
        }
        long nipDuration = (System.nanoTime() - nipStart) / 1_000_000;

        if (nipResult != null && nipResult.decisionTrace() != null && !nipResult.decisionTrace().steps().isEmpty()) {
            DecisionStep nipStep = nipResult.decisionTrace().steps().get(0);
            nipStep = new DecisionStep(
                    nipStep.program(),
                    nipStep.status(),
                    nipDuration,
                    nipStep.matchedConditionId(),
                    nipStep.matchedEmploymentType(),
                    nipStep.matchedSurrogate(),
                    nipStep.computedIncome(),
                    nipStep.effectiveFoir(),
                    nipStep.effectiveRoi(),
                    nipStep.proposedEmi(),
                    nipStep.maxEligibleAmount(),
                    nipStep.ltvDetail(),
                    nipStep.processingFee(),
                    nipStep.loginFee(),
                    nipStep.rules(),
                    nipStep.formulas()
            );
            steps.add(nipStep);
            if (nipStep.status() == DecisionStatus.PASS) {
                stagesPassed++;
            } else {
                stagesFailed++;
            }
        }

        if (nipResult != null && nipResult.isEligible()
                && nipResult.maxEligibleAmount().compareTo(request.loanAmount()) >= 0) {
            log.info("🎯 NIP satisfied requested amount for product={}", product.getProductCode());
            long totalDuration = (System.nanoTime() - cascadeStartTime) / 1_000_000;

            DecisionStep selectedStep = steps.get(0);
            steps.set(0, new DecisionStep(
                    selectedStep.program(),
                    DecisionStatus.SELECTED,
                    selectedStep.executionMillis(),
                    selectedStep.matchedConditionId(),
                    selectedStep.matchedEmploymentType(),
                    selectedStep.matchedSurrogate(),
                    selectedStep.computedIncome(),
                    selectedStep.effectiveFoir(),
                    selectedStep.effectiveRoi(),
                    selectedStep.proposedEmi(),
                    selectedStep.maxEligibleAmount(),
                    selectedStep.ltvDetail(),
                    selectedStep.processingFee(),
                    selectedStep.loginFee(),
                    selectedStep.rules(),
                    selectedStep.formulas()
            ));

            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.PASS,
                    ProgramType.NIP,
                    nipResult.maxEligibleAmount(),
                    nipResult.roi(),
                    nipResult.ltv(),
                    stagesTried, stagesPassed, stagesFailed
            );
            DecisionTrace finalTrace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    totalDuration, buildRequestSnapshot(request), steps, summary
            );

            return new EligibilityResult(
                    nipResult.eligible(), nipResult.productCode(), nipResult.productName(), nipResult.programName(),
                    nipResult.computedMonthlyIncome(), nipResult.effectiveFoir(), nipResult.proposedEmi(),
                    nipResult.maxEligibleAmount(), nipResult.roi(), nipResult.tenureMonths(), nipResult.ltv(),
                    nipResult.ltvDeviated(), nipResult.rejectionReasons(), nipResult.notes(),
                    nipResult.processingFee(), nipResult.loginFee(),
                    nipResult.adminFee(), nipResult.insuranceCharges(), nipResult.legalTechnicalCharges(),
                    nipResult.otherExpense(), nipResult.stampDuty(), nipResult.prepaymentCharges(),
                    nipResult.foreclosureCharges(),
                    nipResult.vintage(), nipResult.negativeProperty(), nipResult.negativeEmployerType(),
                    nipResult.negativeSalaryMode(), nipResult.marginByOccupation(), nipResult.deviationFormulae(),
                    nipResult.conditions(), nipResult.emiNotObligated(), nipResult.bankStatementRequirement(),
                    nipResult.salarySlipRequirement(), nipResult.gstReturnRequirement(), nipResult.providentFundMandatory(),
                    nipResult.itrRequiredYears(), nipResult.profileRestrictions(),
                    finalTrace
            );
        }

        EligibilityResult surrogateResult = null;
        String requestedProgram = (request.incomeComputationInput() != null)
                ? request.incomeComputationInput().programName()
                : null;

        long surrogateDuration = 0;
        if (requestedProgram != null && !"NIP".equalsIgnoreCase(requestedProgram) && !"LOW_LTV".equalsIgnoreCase(requestedProgram) && !"LOW LTV".equalsIgnoreCase(requestedProgram)) {
            long surrogateStart = System.nanoTime();
            stagesTried++;
            try {
                surrogateResult = evaluateProduct(product, request);
            } catch (Exception e) {
                log.warn("Surrogate program '{}' evaluation failed for product={}: {}",
                        requestedProgram, product.getProductCode(), e.getMessage());
            }
            surrogateDuration = (System.nanoTime() - surrogateStart) / 1_000_000;

            if (surrogateResult != null && surrogateResult.decisionTrace() != null && !surrogateResult.decisionTrace().steps().isEmpty()) {
                DecisionStep surrogateStep = surrogateResult.decisionTrace().steps().get(0);
                surrogateStep = new DecisionStep(
                        surrogateStep.program(),
                        surrogateStep.status(),
                        surrogateDuration,
                        surrogateStep.matchedConditionId(),
                        surrogateStep.matchedEmploymentType(),
                        surrogateStep.matchedSurrogate(),
                        surrogateStep.computedIncome(),
                        surrogateStep.effectiveFoir(),
                        surrogateStep.effectiveRoi(),
                        surrogateStep.proposedEmi(),
                        surrogateStep.maxEligibleAmount(),
                        surrogateStep.ltvDetail(),
                        surrogateStep.processingFee(),
                        surrogateStep.loginFee(),
                        surrogateStep.rules(),
                        surrogateStep.formulas()
                );
                steps.add(surrogateStep);
                if (surrogateStep.status() == DecisionStatus.PASS) {
                    stagesPassed++;
                } else {
                    stagesFailed++;
                }
            }
        } else {
            if (requestedProgram != null) {
                try {
                    ProgramType progType = ProgramType.valueOf(requestedProgram.replaceAll("[\\s_-]+", "_").toUpperCase());
                    steps.add(new DecisionStep(
                            progType, DecisionStatus.SKIPPED, 0,
                            null, null, null, null, null, null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of()
                    ));
                } catch (IllegalArgumentException e) {
                    // Fallback for custom programs
                }
            }
        }

        if (surrogateResult != null && surrogateResult.isEligible()
                && surrogateResult.maxEligibleAmount().compareTo(request.loanAmount()) >= 0) {
            log.info("🎯 Surrogate program '{}' satisfied requested amount for product={}",
                    requestedProgram, product.getProductCode());
            long totalDuration = (System.nanoTime() - cascadeStartTime) / 1_000_000;

            int lastIndex = steps.size() - 1;
            DecisionStep selectedStep = steps.get(lastIndex);
            steps.set(lastIndex, new DecisionStep(
                    selectedStep.program(),
                    DecisionStatus.SELECTED,
                    selectedStep.executionMillis(),
                    selectedStep.matchedConditionId(),
                    selectedStep.matchedEmploymentType(),
                    selectedStep.matchedSurrogate(),
                    selectedStep.computedIncome(),
                    selectedStep.effectiveFoir(),
                    selectedStep.effectiveRoi(),
                    selectedStep.proposedEmi(),
                    selectedStep.maxEligibleAmount(),
                    selectedStep.ltvDetail(),
                    selectedStep.processingFee(),
                    selectedStep.loginFee(),
                    selectedStep.rules(),
                    selectedStep.formulas()
            ));

            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.PASS,
                    selectedStep.program(),
                    surrogateResult.maxEligibleAmount(),
                    surrogateResult.roi(),
                    surrogateResult.ltv(),
                    stagesTried, stagesPassed, stagesFailed
            );
            DecisionTrace finalTrace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    totalDuration, buildRequestSnapshot(request), steps, summary
            );

            return new EligibilityResult(
                    surrogateResult.eligible(), surrogateResult.productCode(), surrogateResult.productName(), surrogateResult.programName(),
                    surrogateResult.computedMonthlyIncome(), surrogateResult.effectiveFoir(), surrogateResult.proposedEmi(),
                    surrogateResult.maxEligibleAmount(), surrogateResult.roi(), surrogateResult.tenureMonths(), surrogateResult.ltv(),
                    surrogateResult.ltvDeviated(), surrogateResult.rejectionReasons(), surrogateResult.notes(),
                    surrogateResult.processingFee(), surrogateResult.loginFee(),
                    surrogateResult.adminFee(), surrogateResult.insuranceCharges(), surrogateResult.legalTechnicalCharges(),
                    surrogateResult.otherExpense(), surrogateResult.stampDuty(), surrogateResult.prepaymentCharges(),
                    surrogateResult.foreclosureCharges(),
                    surrogateResult.vintage(), surrogateResult.negativeProperty(), surrogateResult.negativeEmployerType(),
                    surrogateResult.negativeSalaryMode(), surrogateResult.marginByOccupation(), surrogateResult.deviationFormulae(),
                    surrogateResult.conditions(), surrogateResult.emiNotObligated(), surrogateResult.bankStatementRequirement(),
                    surrogateResult.salarySlipRequirement(), surrogateResult.gstReturnRequirement(), surrogateResult.providentFundMandatory(),
                    surrogateResult.itrRequiredYears(), surrogateResult.profileRestrictions(),
                    finalTrace
            );
        }

        EligibilityResult lowLtvResult = null;
        long lowLtvStart = System.nanoTime();
        stagesTried++;
        try {
            lowLtvResult = evaluateProductLowLtv(product, request);
        } catch (Exception e) {
            log.error("Low LTV evaluation failed for product={}: {}", product.getProductCode(), e.getMessage(), e);
        }
        long lowLtvDuration = (System.nanoTime() - lowLtvStart) / 1_000_000;

        if (lowLtvResult != null && lowLtvResult.decisionTrace() != null && !lowLtvResult.decisionTrace().steps().isEmpty()) {
            DecisionStep lowLtvStep = lowLtvResult.decisionTrace().steps().get(0);
            lowLtvStep = new DecisionStep(
                    lowLtvStep.program(),
                    lowLtvStep.status(),
                    lowLtvDuration,
                    lowLtvStep.matchedConditionId(),
                    lowLtvStep.matchedEmploymentType(),
                    lowLtvStep.matchedSurrogate(),
                    lowLtvStep.computedIncome(),
                    lowLtvStep.effectiveFoir(),
                    lowLtvStep.effectiveRoi(),
                    lowLtvStep.proposedEmi(),
                    lowLtvStep.maxEligibleAmount(),
                    lowLtvStep.ltvDetail(),
                    lowLtvStep.processingFee(),
                    lowLtvStep.loginFee(),
                    lowLtvStep.rules(),
                    lowLtvStep.formulas()
            );
            steps.add(lowLtvStep);
            if (lowLtvStep.status() == DecisionStatus.PASS) {
                stagesPassed++;
            } else {
                stagesFailed++;
            }
        }

        if (lowLtvResult != null && lowLtvResult.isEligible()) {
            log.info("🎯 Low LTV surrogate satisfied for product={}", product.getProductCode());
            long totalDuration = (System.nanoTime() - cascadeStartTime) / 1_000_000;

            int lastIndex = steps.size() - 1;
            DecisionStep selectedStep = steps.get(lastIndex);
            steps.set(lastIndex, new DecisionStep(
                    selectedStep.program(),
                    DecisionStatus.SELECTED,
                    selectedStep.executionMillis(),
                    selectedStep.matchedConditionId(),
                    selectedStep.matchedEmploymentType(),
                    selectedStep.matchedSurrogate(),
                    selectedStep.computedIncome(),
                    selectedStep.effectiveFoir(),
                    selectedStep.effectiveRoi(),
                    selectedStep.proposedEmi(),
                    selectedStep.maxEligibleAmount(),
                    selectedStep.ltvDetail(),
                    selectedStep.processingFee(),
                    selectedStep.loginFee(),
                    selectedStep.rules(),
                    selectedStep.formulas()
            ));

            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.PASS,
                    ProgramType.LOW_LTV,
                    lowLtvResult.maxEligibleAmount(),
                    lowLtvResult.roi(),
                    lowLtvResult.ltv(),
                    stagesTried, stagesPassed, stagesFailed
            );
            DecisionTrace finalTrace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    totalDuration, buildRequestSnapshot(request), steps, summary
            );

            return new EligibilityResult(
                    lowLtvResult.eligible(), lowLtvResult.productCode(), lowLtvResult.productName(), lowLtvResult.programName(),
                    lowLtvResult.computedMonthlyIncome(), lowLtvResult.effectiveFoir(), lowLtvResult.proposedEmi(),
                    lowLtvResult.maxEligibleAmount(), lowLtvResult.roi(), lowLtvResult.tenureMonths(), lowLtvResult.ltv(),
                    lowLtvResult.ltvDeviated(), lowLtvResult.rejectionReasons(), lowLtvResult.notes(),
                    lowLtvResult.processingFee(), lowLtvResult.loginFee(),
                    lowLtvResult.adminFee(), lowLtvResult.insuranceCharges(), lowLtvResult.legalTechnicalCharges(),
                    lowLtvResult.otherExpense(), lowLtvResult.stampDuty(), lowLtvResult.prepaymentCharges(),
                    lowLtvResult.foreclosureCharges(),
                    lowLtvResult.vintage(), lowLtvResult.negativeProperty(), lowLtvResult.negativeEmployerType(),
                    lowLtvResult.negativeSalaryMode(), lowLtvResult.marginByOccupation(), lowLtvResult.deviationFormulae(),
                    lowLtvResult.conditions(), lowLtvResult.emiNotObligated(), lowLtvResult.bankStatementRequirement(),
                    lowLtvResult.salarySlipRequirement(), lowLtvResult.gstReturnRequirement(), lowLtvResult.providentFundMandatory(),
                    lowLtvResult.itrRequiredYears(), lowLtvResult.profileRestrictions(),
                    finalTrace
            );
        }

        EligibilityResult bestResult = selectBestFallback(nipResult, surrogateResult, lowLtvResult);
        long totalDuration = (System.nanoTime() - cascadeStartTime) / 1_000_000;

        DecisionStatus finalStatus = bestResult.isEligible() ? DecisionStatus.PASS : DecisionStatus.REJECTED;
        ProgramType selectedProgram = null;
        if (bestResult.isEligible()) {
            if ("LOW_LTV".equalsIgnoreCase(bestResult.programName()) || "LOW LTV".equalsIgnoreCase(bestResult.programName())) {
                selectedProgram = ProgramType.LOW_LTV;
            } else if (bestResult.programName() != null) {
                try {
                    selectedProgram = ProgramType.valueOf(bestResult.programName().replaceAll("[\\s_-]+", "_").toUpperCase());
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }

        DecisionSummary summary = new DecisionSummary(
                finalStatus,
                selectedProgram,
                bestResult.isEligible() ? bestResult.maxEligibleAmount() : BigDecimal.ZERO,
                bestResult.isEligible() ? bestResult.roi() : BigDecimal.ZERO,
                bestResult.isEligible() ? bestResult.ltv() : BigDecimal.ZERO,
                stagesTried, stagesPassed, stagesFailed
        );
        DecisionTrace finalTrace = new DecisionTrace(
                UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                totalDuration, buildRequestSnapshot(request), steps, summary
        );

        return new EligibilityResult(
                bestResult.eligible(), bestResult.productCode(), bestResult.productName(), bestResult.programName(),
                bestResult.computedMonthlyIncome(), bestResult.effectiveFoir(), bestResult.proposedEmi(),
                bestResult.maxEligibleAmount(), bestResult.roi(), bestResult.tenureMonths(), bestResult.ltv(),
                bestResult.ltvDeviated(), bestResult.rejectionReasons(), bestResult.notes(),
                bestResult.processingFee(), bestResult.loginFee(),
                bestResult.adminFee(), bestResult.insuranceCharges(), bestResult.legalTechnicalCharges(),
                bestResult.otherExpense(), bestResult.stampDuty(), bestResult.prepaymentCharges(),
                bestResult.foreclosureCharges(),
                bestResult.vintage(), bestResult.negativeProperty(), bestResult.negativeEmployerType(),
                bestResult.negativeSalaryMode(), bestResult.marginByOccupation(), bestResult.deviationFormulae(),
                bestResult.conditions(), bestResult.emiNotObligated(), bestResult.bankStatementRequirement(),
                bestResult.salarySlipRequirement(), bestResult.gstReturnRequirement(), bestResult.providentFundMandatory(),
                bestResult.itrRequiredYears(), bestResult.profileRestrictions(),
                finalTrace
        );
    }

    private IncomeComputationInput getIncomeInputForProgram(EligibilityRequest request, String programName) {
        if (request.incomeComputationInput() != null
                && programName.equalsIgnoreCase(request.incomeComputationInput().programName())) {
            return request.incomeComputationInput();
        }
        if ("NIP".equalsIgnoreCase(programName)) {
            BigDecimal monthly = request.monthlyIncome();
            if ((monthly == null || monthly.compareTo(BigDecimal.ZERO) <= 0) && request.grossMonthlyIncome() != null) {
                monthly = request.grossMonthlyIncome();
            }
            BigDecimal pat = (monthly != null) ? monthly.multiply(new BigDecimal("12")) : BigDecimal.ZERO;
            return new IncomeComputationInput(
                    "NIP",
                    pat,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    List.of(),
                    BigDecimal.ZERO,
                    "",
                    BigDecimal.ZERO,
                    "",
                    "", // lenderName — not needed for NIP
                    "" // loanType — not needed for NIP
            );
        }
        return request.incomeComputationInput();
    }

    private EligibilityResult selectBestFallback(EligibilityResult nip, EligibilityResult surrogate,
            EligibilityResult lowLtv) {
        List<EligibilityResult> list = new ArrayList<>();
        if (nip != null)
            list.add(nip);
        if (surrogate != null)
            list.add(surrogate);
        if (lowLtv != null)
            list.add(lowLtv);

        if (list.isEmpty()) {
            return EligibilityResult.rejected(List.of("Eligibility computation failed for all cascade programs"),
                    "All programs in cascade failed");
        }

        return list.stream()
                .max(Comparator.comparing(EligibilityResult::isEligible)
                        .thenComparing(EligibilityResult::maxEligibleAmount)
                        .thenComparing(r -> -r.rejectionReasons().size()))
                .orElse(nip != null ? nip : (surrogate != null ? surrogate : lowLtv));
    }

    private EligibilityResult evaluateProductLowLtv(LoanProduct product, EligibilityRequest request) {
        long startTime = System.nanoTime();
        var allConditions = eligibilityConditionRepository.findByProductId(product.getId());

        final String normalizedEmpType = normalizeEmploymentType(
                request.employmentType() != null ? request.employmentType() : null);

        var conditions = allConditions.stream()
                .filter(c -> c.getSurrogate() == null
                        || c.getSurrogate().isBlank()
                        || c.getSurrogate().equalsIgnoreCase("NIP")
                        || c.getSurrogate().equalsIgnoreCase("LOW_LTV")
                        || c.getSurrogate().equalsIgnoreCase("LOW LTV"))
                .filter(c -> matchEmploymentType(c.getEmploymentType(), normalizedEmpType))
                .toList();

        List<RuleEvaluation> rulesEvaluated = new ArrayList<>();
        List<FormulaTrace> formulasEvaluated = new ArrayList<>();

        if (conditions.isEmpty()) {
            List<String> reasons = List.of(String.format("No eligibility lane for Low LTV: empType=%s (normalized=%s)",
                    request.employmentType(), normalizedEmpType));

            rulesEvaluated.add(new RuleEvaluation(
                    "CONDITION_LANE",
                    DecisionStatus.FAIL,
                    "Low LTV conditions exist matching empType",
                    false,
                    "No Low LTV condition lane matched applicant profile"
            ));

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            DecisionStep step = new DecisionStep(
                    ProgramType.LOW_LTV,
                    DecisionStatus.FAIL,
                    duration,
                    null, null, null,
                    BigDecimal.ZERO, null, null, null, null,
                    null, BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    ProgramType.LOW_LTV,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return new EligibilityResult(
                    false, product.getProductCode(), product.getLenderName(), "LOW_LTV",
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    false, reasons, "No matching condition lane for applicant profile",
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    trace
            );
        }

        Map<String, Object> applicantPayload = new HashMap<>();
        BigDecimal incomeForFloorCheck = request.monthlyIncome();
        if (incomeForFloorCheck == null || incomeForFloorCheck.compareTo(BigDecimal.ZERO) <= 0) {
            incomeForFloorCheck = request.grossMonthlyIncome();
        }
        final BigDecimal effectiveIncome = incomeForFloorCheck != null ? incomeForFloorCheck : BigDecimal.ZERO;
        applicantPayload.put("cibilScore", request.cibilScore());
        applicantPayload.put("loanAmount", request.loanAmount());
        applicantPayload.put("propertyValue", request.propertyValue());
        applicantPayload.put("existingEmiTotal", request.existingEmiTotal());
        applicantPayload.put("requestedTenureMonths", request.requestedTenureMonths());
        applicantPayload.put("applicantAge", request.applicantAge());
        applicantPayload.put("cityTier", request.cityTier());
        applicantPayload.put("propertyType", request.propertyType());
        applicantPayload.put("businessAgeYears", request.businessAgeYears());
        applicantPayload.put("workExpYears", request.workExpYears());
        applicantPayload.put("employmentType", request.employmentType());

        final String resolvedPropertyCategory = resolvePropertyCategory(request.propertyType());
        final String rawPropertySubType = request.propertyType() != null ? request.propertyType().toUpperCase()
                : "RESIDENTIAL";
        applicantPayload.put("propertyCategory", resolvedPropertyCategory);

        EligibilityCondition matchedCondition = null;
        List<String> allRejectionReasons = new ArrayList<>();

        for (var c : conditions) {
            List<String> reasonsForThisCondition = new ArrayList<>();
            List<RuleEvaluation> laneRules = new ArrayList<>();

            if (c.getMinAge() != null) {
                boolean pass = request.applicantAge() >= c.getMinAge();
                laneRules.add(new RuleEvaluation(
                        "MIN_AGE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMinAge(),
                        request.applicantAge(),
                        pass ? "Age meets min age limit" : String.format("applicantAge=%d < minAge=%d", request.applicantAge(), c.getMinAge())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("AGE_TOO_LOW: applicantAge=%d < minAge=%d",
                            request.applicantAge(), c.getMinAge()));
                }
            }
            double ageAtMaturity = request.applicantAge() + (request.requestedTenureMonths() / 12.0);
            if (c.getMaxAge() != null) {
                boolean pass = ageAtMaturity <= c.getMaxAge();
                laneRules.add(new RuleEvaluation(
                        "MAX_AGE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMaxAge(),
                        ageAtMaturity,
                        pass ? "Age at maturity meets max age limit" : String.format("ageAtMaturity=%.2f > maxAge=%d", ageAtMaturity, c.getMaxAge())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("AGE_TOO_HIGH: ageAtMaturity=%.2f > maxAge=%d",
                            ageAtMaturity, c.getMaxAge()));
                }
            }
            if (c.getBusinessAgeYears() != null) {
                boolean pass = request.businessAgeYears() >= c.getBusinessAgeYears();
                laneRules.add(new RuleEvaluation(
                        "BUSINESS_AGE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getBusinessAgeYears(),
                        request.businessAgeYears(),
                        pass ? "Business age meets limit" : String.format("businessAge=%d < required=%d", request.businessAgeYears(), c.getBusinessAgeYears())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("BIZ_VINTAGE_LOW: businessAge=%d < required=%d",
                            request.businessAgeYears(), c.getBusinessAgeYears()));
                }
            }
            if (c.getWorkExpYears() != null) {
                boolean pass = request.workExpYears() >= c.getWorkExpYears();
                laneRules.add(new RuleEvaluation(
                        "WORK_EXP",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getWorkExpYears(),
                        request.workExpYears(),
                        pass ? "Work experience meets limit" : String.format("workExp=%d < required=%d", request.workExpYears(), c.getWorkExpYears())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("WORK_EXP_LOW: workExp=%d < required=%d",
                            request.workExpYears(), c.getWorkExpYears()));
                }
            }

            if (c.getPropertyType() != null) {
                String allowedRaw = c.getPropertyType().toUpperCase();
                boolean propertyAllowed = Arrays.stream(allowedRaw.split("[,;]"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .anyMatch(allowed -> allowed.equalsIgnoreCase(resolvedPropertyCategory)
                                || allowed.equalsIgnoreCase(rawPropertySubType));
                laneRules.add(new RuleEvaluation(
                        "PROPERTY_TYPE",
                        propertyAllowed ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getPropertyType(),
                        rawPropertySubType,
                        propertyAllowed ? "Property type allowed" : String.format("subType=%s not allowed by list: %s", rawPropertySubType, c.getPropertyType())
                ));
                if (!propertyAllowed) {
                    reasonsForThisCondition.add(String.format("PROPERTY_NOT_ALLOWED: subType=%s, resolved=%s",
                            rawPropertySubType, resolvedPropertyCategory));
                }
            }

            if (c.getCityTier() != null) {
                boolean pass = c.getCityTier().equalsIgnoreCase(request.cityTier());
                laneRules.add(new RuleEvaluation(
                        "CITY_TIER",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getCityTier(),
                        request.cityTier(),
                        pass ? "City tier matches" : String.format("requested=%s, required=%s", request.cityTier(), c.getCityTier())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("CITY_TIER_MISMATCH: requested=%s, required=%s",
                            request.cityTier(), c.getCityTier()));
                }
            }
            if (c.getCibilMin() != null) {
                boolean pass = request.cibilScore() >= c.getCibilMin();
                laneRules.add(new RuleEvaluation(
                        "MIN_CIBIL",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getCibilMin(),
                        request.cibilScore(),
                        pass ? "CIBIL score meets minimum limit" : String.format("score=%d < required=%d", request.cibilScore(), c.getCibilMin())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(
                            String.format("CIBIL_TOO_LOW: score=%d < required=%d", request.cibilScore(), c.getCibilMin()));
                }
            }


            if (c.getMinLoanAmount() != null) {
                boolean pass = request.loanAmount() != null && request.loanAmount().compareTo(c.getMinLoanAmount()) >= 0;
                laneRules.add(new RuleEvaluation(
                        "MIN_LOAN_AMOUNT",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMinLoanAmount(),
                        request.loanAmount(),
                        pass ? "Loan amount meets min limit" : String.format("amount=%s < min=%s", request.loanAmount(), c.getMinLoanAmount())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("LOAN_AMOUNT_TOO_LOW: amount=%s < min=%s",
                            request.loanAmount(), c.getMinLoanAmount()));
                }
            }
            if (c.getMaxLoanAmount() != null) {
                boolean pass = request.loanAmount() != null && request.loanAmount().compareTo(c.getMaxLoanAmount()) <= 0;
                laneRules.add(new RuleEvaluation(
                        "MAX_LOAN_AMOUNT",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMaxLoanAmount(),
                        request.loanAmount(),
                        pass ? "Loan amount meets max limit" : String.format("amount=%s > max=%s", request.loanAmount(), c.getMaxLoanAmount())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("LOAN_AMOUNT_TOO_HIGH: amount=%s > max=%s",
                            request.loanAmount(), c.getMaxLoanAmount()));
                }
            }
            if (c.getMinTenure() != null) {
                boolean pass = request.requestedTenureMonths() >= c.getMinTenure();
                laneRules.add(new RuleEvaluation(
                        "MIN_TENURE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMinTenure(),
                        request.requestedTenureMonths(),
                        pass ? "Tenure meets min limit" : String.format("tenure=%d < min=%d", request.requestedTenureMonths(), c.getMinTenure())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("TENURE_TOO_LOW: tenure=%d < min=%d",
                            request.requestedTenureMonths(), c.getMinTenure()));
                }
            }
            if (c.getMaxTenure() != null) {
                boolean pass = request.requestedTenureMonths() <= c.getMaxTenure();
                laneRules.add(new RuleEvaluation(
                        "MAX_TENURE",
                        pass ? DecisionStatus.PASS : DecisionStatus.FAIL,
                        c.getMaxTenure(),
                        request.requestedTenureMonths(),
                        pass ? "Tenure meets max limit" : String.format("tenure=%d > max=%d", request.requestedTenureMonths(), c.getMaxTenure())
                ));
                if (!pass) {
                    reasonsForThisCondition.add(String.format("TENURE_TOO_HIGH: tenure=%d > max=%d",
                            request.requestedTenureMonths(), c.getMaxTenure()));
                }
            }

            if (!smartEvaluate(c.getProfileRestrictions(), applicantPayload, "employmentType")) {
                reasonsForThisCondition.add(String.format("PROFILE_RESTRICTED: empType=%s", request.employmentType()));
            }
            if (!smartEvaluatePropertyDenyList(c.getNegativeProperty(), rawPropertySubType, resolvedPropertyCategory)) {
                reasonsForThisCondition.add(String.format("NEGATIVE_PROPERTY: subType=%s", rawPropertySubType));
            }
            if (!smartEvaluate(c.getConditions(), applicantPayload, null)) {
                reasonsForThisCondition.add("CONDITION_RULE_FAILED");
            }
            if (!smartEvaluate(c.getDeviationFormulae(), applicantPayload, null)) {
                reasonsForThisCondition.add("DEVIATION_RULE_FAILED");
            }

            rulesEvaluated.addAll(laneRules);

            if (reasonsForThisCondition.isEmpty()) {
                matchedCondition = c;
                break;
            }
            allRejectionReasons.addAll(reasonsForThisCondition);
        }

        if (matchedCondition == null) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            DecisionStep step = new DecisionStep(
                    ProgramType.LOW_LTV,
                    DecisionStatus.FAIL,
                    duration,
                    null, null, null,
                    effectiveIncome, null, null, null, null,
                    null, BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    ProgramType.LOW_LTV,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return new EligibilityResult(
                    false, product.getProductCode(), product.getLenderName(), "LOW_LTV",
                    effectiveIncome, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    false, allRejectionReasons, "Applicant profile does not satisfy standard checks under Low LTV",
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    trace
            );
        }

        BigDecimal effectiveLtv = matchedCondition.getLtvAllowed();
        String sourceLtv = "DB:condition_id=" + matchedCondition.getId();
        if (effectiveLtv == null) {
            if ("HL".equalsIgnoreCase(product.getLoanType())) {
                effectiveLtv = lowLtvSurrogateService.getHlLtv(request.propertyType(), request.loanAmount());
                sourceLtv = "HARDCODED:HL_LTV_GRID";
            } else if ("LAP".equalsIgnoreCase(product.getLoanType())) {
                String propertyKey = lowLtvSurrogateService.resolvePropertyKey(
                        request.propertyType(),
                        request.propertyCategory(),
                        request.businessPropertyCategory());
                effectiveLtv = lowLtvSurrogateService.getLapLtv(product.getLenderName(), propertyKey);
                sourceLtv = "HARDCODED:LAP_LTV_MATRIX";
            }
        }

        if (effectiveLtv == null) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            rulesEvaluated.add(new RuleEvaluation(
                    "LTV_RESOLVED",
                    DecisionStatus.FAIL,
                    "LTV resolved to non-null value",
                    false,
                    "LTV resolved is null"
            ));
            DecisionStep step = new DecisionStep(
                    ProgramType.LOW_LTV,
                    DecisionStatus.FAIL,
                    duration,
                    matchedCondition.getId(), matchedCondition.getEmploymentType(), matchedCondition.getSurrogate(),
                    effectiveIncome, null, null, null, null,
                    null, BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    ProgramType.LOW_LTV,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return new EligibilityResult(
                    false, product.getProductCode(), product.getLenderName(), "LOW_LTV",
                    effectiveIncome, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                    false, List.of("Property type not allowed under Low LTV Surrogate for this lender"),
                    "Ltv matrix returned negative or empty allowed Ltv",
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    trace
            );
        }

        ApplicantProfile applicantProfile = new ApplicantProfile(request.cibilScore(), normalizedEmpType,
                BigDecimal.ZERO);
        final BigDecimal effectiveRoi = financialComputationEngine.resolveRoi(product, applicantProfile,
                request.loanAmount());

        var proposedEmi = calculateProposedEmiWithRate(request.loanAmount(), effectiveRoi,
                request.requestedTenureMonths());

        formulasEvaluated.add(new FormulaTrace(
                "EMI",
                "principal * [r(1+r)^n] / [(1+r)^n - 1]",
                Map.of(
                        "principal", request.loanAmount(),
                        "roi", effectiveRoi,
                        "tenureMonths", request.requestedTenureMonths()
                ),
                proposedEmi
        ));

        BigDecimal ltvCap = request.propertyValue().multiply(effectiveLtv, MathContext.DECIMAL128);

        rulesEvaluated.add(new RuleEvaluation(
                "LTV_LIMIT",
                request.loanAmount().compareTo(ltvCap) <= 0 ? DecisionStatus.PASS : DecisionStatus.FAIL,
                "<=" + ltvCap.setScale(2, RoundingMode.HALF_UP),
                request.loanAmount(),
                request.loanAmount().compareTo(ltvCap) <= 0 ? "LTV within limit" : "LTV exceeded limit"
        ));

        formulasEvaluated.add(new FormulaTrace(
                "LTV_CAP",
                "propertyValue * effectiveLtv",
                Map.of(
                        "propertyValue", request.propertyValue(),
                        "effectiveLtv", effectiveLtv
                ),
                ltvCap
        ));

        if (request.loanAmount().compareTo(ltvCap) > 0) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            DecisionStep step = new DecisionStep(
                    ProgramType.LOW_LTV,
                    DecisionStatus.FAIL,
                    duration,
                    matchedCondition.getId(), matchedCondition.getEmploymentType(), matchedCondition.getSurrogate(),
                    effectiveIncome, null, effectiveRoi, proposedEmi, BigDecimal.ZERO,
                    new LtvDetail(request.propertyValue(), effectiveLtv, "propertyValue * effectiveLtv", ltvCap, sourceLtv),
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    rulesEvaluated, formulasEvaluated
            );
            DecisionSummary summary = new DecisionSummary(
                    DecisionStatus.FAIL,
                    ProgramType.LOW_LTV,
                    BigDecimal.ZERO, effectiveRoi, effectiveLtv,
                    1, 0, 1
            );
            DecisionTrace trace = new DecisionTrace(
                    UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                    duration, buildRequestSnapshot(request), List.of(step), summary
            );

            return new EligibilityResult(
                    false, product.getProductCode(), product.getLenderName(), "LOW_LTV",
                    effectiveIncome, BigDecimal.ZERO, proposedEmi,
                    BigDecimal.ZERO, effectiveRoi, request.requestedTenureMonths(), effectiveLtv,
                    false, List.of(String.format("LOW_LTV_EXCEEDED: requested %.2f, allowed %.2f (LTV limit: %.0f%%)",
                            request.loanAmount(), ltvCap, effectiveLtv.multiply(BigDecimal.valueOf(100)))),
                    "Requested loan amount exceeds the Low LTV surrogate limit",
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    trace
            );
        }

        BigDecimal finalLoanAmount = request.loanAmount().min(ltvCap);

        final BigDecimal processingFee = financialComputationEngine.resolveProcessingFee(
                product, finalLoanAmount, normalizedEmpType);
        final BigDecimal loginFee = financialComputationEngine.resolveLoginFee(
                product, finalLoanAmount, normalizedEmpType);

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        DecisionStep step = new DecisionStep(
                ProgramType.LOW_LTV,
                DecisionStatus.PASS,
                duration,
                matchedCondition.getId(), matchedCondition.getEmploymentType(), matchedCondition.getSurrogate(),
                effectiveIncome, BigDecimal.ZERO, effectiveRoi, proposedEmi, finalLoanAmount,
                new LtvDetail(request.propertyValue(), effectiveLtv, "propertyValue * effectiveLtv", ltvCap, sourceLtv),
                processingFee, loginFee,
                rulesEvaluated, formulasEvaluated
        );
        DecisionSummary summary = new DecisionSummary(
                DecisionStatus.PASS,
                ProgramType.LOW_LTV,
                finalLoanAmount, effectiveRoi, effectiveLtv,
                1, 1, 0
        );
        DecisionTrace trace = new DecisionTrace(
                UUID.randomUUID(), Instant.now(), ENGINE_VERSION, masterDataVersionService.computeVersion(),
                duration, buildRequestSnapshot(request), List.of(step), summary
            );

        return new EligibilityResult(
                true, product.getProductCode(), product.getLenderName(), "LOW_LTV",
                effectiveIncome, BigDecimal.ZERO, proposedEmi,
                finalLoanAmount, effectiveRoi, request.requestedTenureMonths(), effectiveLtv,
                false, List.of(), "Eligible (Low LTV Surrogate)",
                processingFee, loginFee,
                product.getAdminFee(), product.getInsuranceCharges(), product.getLegalTechnicalCharges(),
                product.getOtherExpense(), product.getStampDuties(), product.getPrepaymentCharges(),
                product.getForeclosureCharges(),
                matchedCondition.getVintage(), matchedCondition.getNegativeProperty(), matchedCondition.getNegativeEmployerType(),
                matchedCondition.getNegativeSalaryMode(), matchedCondition.getMarginByOccupation(), matchedCondition.getDeviationFormulae(),
                matchedCondition.getConditions(), matchedCondition.getEmiNotObligated(), matchedCondition.getBankStatementRequirement(),
                matchedCondition.getSalarySlipRequirement(), matchedCondition.getGstReturnRequirement(), matchedCondition.getProvidentFundMandatory(),
                matchedCondition.getItrRequiredYears(), matchedCondition.getProfileRestrictions(),
                trace
        );
    }

    private boolean isProductAllowedForEmploymentType(String productCode, String lenderName, String rawEmpType) {
        if (rawEmpType == null) {
            return true;
        }

        String empType = rawEmpType.toUpperCase();
        String code = productCode != null ? productCode.toUpperCase() : "";
        String lender = lenderName != null ? lenderName.toUpperCase() : "";

        if (empType.equals("SALARIED")) {
            // Exclude products that are self-employed only
            if (code.endsWith("-0002") || code.endsWith("-0003") || code.endsWith("-SEP") || code.endsWith("-SENP")) {
                return false;
            }
        } else if (empType.equals("SELF_EMPLOYED")) {
            // Exclude products that are salaried (or salaried/SEP)
            if (code.endsWith("-0001") || code.endsWith("-SAL") || code.endsWith("-SALARIED")) {
                return false;
            }
        } else if (empType.equals("PROFESSIONAL")) {
            // For Bajaj and L&T, professional matches self-employed products
            if (lender.contains("BAJAJ") || lender.contains("L&T") || code.startsWith("BAJAJ-")
                    || code.startsWith("LT-") || code.startsWith("LT_")) {
                if (code.endsWith("-0001") || code.endsWith("-SAL") || code.endsWith("-SALARIED")) {
                    return false;
                }
            } else {
                // For other lenders, professional matches salaried/SEP products (which is
                // -0001)
                if (code.endsWith("-0002") || code.endsWith("-0003") || code.endsWith("-SEP")
                        || code.endsWith("-SENP")) {
                    return false;
                }
            }
        }

        return true;
    }

    private FormulaTrace traceIncome(IncomeComputationInput input, BigDecimal output) {
        if (input == null || input.programName() == null) {
            return new FormulaTrace("INCOME", "Declared Income", Map.of(), output);
        }
        Map<String, Object> inputs = new HashMap<>();
        String expr = "";
        switch (input.programName().toUpperCase()) {
            case "NIP" -> {
                inputs.put("pat", input.pat() != null ? input.pat() : BigDecimal.ZERO);
                inputs.put("depreciation", input.depreciation() != null ? input.depreciation() : BigDecimal.ZERO);
                inputs.put("interestExpense", input.interestExpense() != null ? input.interestExpense() : BigDecimal.ZERO);
                expr = "(pat + depreciation + interestExpense) / 12";
            }
            case "BANKING", "CASHFLOW" -> {
                if (input.bankBalanceSamples() != null && !input.bankBalanceSamples().isEmpty()) {
                    inputs.put("bankBalanceSamples", input.bankBalanceSamples());
                    expr = "average(bankBalanceSamples)";
                } else {
                    inputs.put("averageBankBalance", input.averageBankBalance() != null ? input.averageBankBalance() : BigDecimal.ZERO);
                    expr = "averageBankBalance";
                }
            }
            case "GST" -> {
                inputs.put("gstrTurnover12Months", input.gstrTurnover12Months() != null ? input.gstrTurnover12Months() : BigDecimal.ZERO);
                inputs.put("businessType", input.businessType() != null ? input.businessType() : "");
                inputs.put("lenderName", input.lenderName() != null ? input.lenderName() : "");
                inputs.put("loanType", input.loanType() != null ? input.loanType() : "");
                expr = "(gstrTurnover12Months * margin) / 12";
            }
            case "SENP" -> {
                inputs.put("grossReceipts", input.grossReceipts() != null ? input.grossReceipts() : BigDecimal.ZERO);
                inputs.put("profession", input.profession() != null ? input.profession() : "");
                expr = "(grossReceipts * multiplier) / 12";
            }
            case "SEP" -> {
                inputs.put("grossReceipts", input.grossReceipts() != null ? input.grossReceipts() : BigDecimal.ZERO);
                inputs.put("profession", input.profession() != null ? input.profession() : "");
                inputs.put("lenderName", input.lenderName() != null ? input.lenderName() : "");
                inputs.put("loanType", input.loanType() != null ? input.loanType() : "");
                expr = "(grossReceipts * multiplier) / 12";
            }
            case "CPM_SEP", "CPM SEP", "CPM" -> {
                inputs.put("pat", input.pat() != null ? input.pat() : BigDecimal.ZERO);
                inputs.put("depreciation", input.depreciation() != null ? input.depreciation() : BigDecimal.ZERO);
                inputs.put("grossReceipts", input.grossReceipts() != null ? input.grossReceipts() : BigDecimal.ZERO);
                inputs.put("profession", input.profession() != null ? input.profession() : "");
                inputs.put("lenderName", input.lenderName() != null ? input.lenderName() : "");
                inputs.put("loanType", input.loanType() != null ? input.loanType() : "");
                expr = "min((pat + depreciation) * multiplier, grossReceipts) / 12";
            }
            default -> expr = "unknown";
        }
        return new FormulaTrace("INCOME", expr, inputs, output);
    }

    private EligibilityRequestSnapshot buildRequestSnapshot(EligibilityRequest request) {
        return new EligibilityRequestSnapshot(
                request.cibilScore(),
                request.applicantAge(),
                request.employmentType(),
                request.propertyType(),
                request.loanAmount(),
                request.propertyValue(),
                request.requestedTenureMonths(),
                request.monthlyIncome(),
                request.existingEmiTotal(),
                request.loanType(),
                request.incomeComputationInput() != null ? request.incomeComputationInput().programName() : null
        );
    }

    private EligibilityResult buildPreflightRejectedResult(EligibilityRequest request, List<String> reasons, String notes) {
        EligibilityRequestSnapshot snapshot = buildRequestSnapshot(request);
        List<DecisionStep> steps = List.of(new DecisionStep(
                ProgramType.NIP,
                DecisionStatus.FAIL,
                0,
                null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, // LtvDetail
                BigDecimal.ZERO, BigDecimal.ZERO, // fees
                reasons.stream().map(r -> new RuleEvaluation("PRE_FLIGHT_FAIL", DecisionStatus.FAIL, null, null, r)).toList(),
                List.of()
        ));
        DecisionSummary summary = new DecisionSummary(
                DecisionStatus.REJECTED,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1, 0, 1
        );
        DecisionTrace trace = new DecisionTrace(
                UUID.randomUUID(),
                Instant.now(),
                ENGINE_VERSION,
                masterDataVersionService.computeVersion(),
                0,
                snapshot,
                steps,
                summary
        );
        return new EligibilityResult(
                false, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                false, reasons, notes, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                trace
        );
    }

    private ProgramType parseProgramType(String name) {
        if (name == null || name.isBlank()) {
            return ProgramType.NIP;
        }
        try {
            return ProgramType.valueOf(name.replaceAll("[\\s_-]+", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown program type name: {}. Falling back to NIP.", name);
            return ProgramType.NIP;
        }
    }
}
