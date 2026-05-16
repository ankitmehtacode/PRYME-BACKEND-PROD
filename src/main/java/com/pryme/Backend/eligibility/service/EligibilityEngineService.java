// File: src/main/java/com/pryme/Backend/eligibility/service/EligibilityEngineService.java

package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import com.pryme.Backend.common.repository.PolicyFieldDefinitionRepository;
import com.pryme.Backend.eligibility.dto.ApplicantProfile;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
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
            Map.entry("LAND", "LAND")
    );

    // ── LOAN TYPE NORMALIZATION ──────────────────────────────────────────────
    // Frontend sends full names (HOME_LOAN, LOAN_AGAINST_PROPERTY).
    // V19 DB stores short codes (HL, LAP, BL, PL).
    private static final Map<String, String> LOAN_TYPE_NORMALIZATION = Map.of(
            "HOME_LOAN", "HL",
            "LOAN_AGAINST_PROPERTY", "LAP",
            "BUSINESS_LOAN", "BL",
            "PERSONAL_LOAN", "PL",
            "CREDIT_CARD", "CC"
    );

    // ── EMPLOYMENT TYPE NORMALIZATION ────────────────────────────────────────
    // Frontend sends SALARIED, SELF_EMPLOYED, PROFESSIONAL.
    // V19 DB stores Salaried, SEP/SENP.
    private static final Map<String, String> EMPLOYMENT_TYPE_NORMALIZATION = Map.of(
            "SELF_EMPLOYED", "SEP/SENP",
            "PROFESSIONAL", "SEP/SENP",
            "SALARIED", "Salaried"
    );

    /**
     * Resolve a frontend property sub-type to its bank-policy category.
     * Falls back to the input itself if no mapping exists (forward-compatible).
     */
    private static String resolvePropertyCategory(String propertyType) {
        if (propertyType == null) return "RESIDENTIAL";
        return PROPERTY_SUBTYPE_TO_CATEGORY.getOrDefault(propertyType.toUpperCase(), propertyType.toUpperCase());
    }

    /**
     * Resolve a frontend loanType to its DB short code.
     * Falls back to the input itself if no mapping exists (forward-compatible).
     */
    private static String normalizeLoanType(String loanType) {
        if (loanType == null) return "HL";
        return LOAN_TYPE_NORMALIZATION.getOrDefault(loanType.toUpperCase(), loanType.toUpperCase());
    }

    /**
     * Resolve a frontend employmentType to its DB value.
     * Falls back to the input itself if no mapping exists (forward-compatible).
     */
    private static String normalizeEmploymentType(String empType) {
        if (empType == null) return null;
        return EMPLOYMENT_TYPE_NORMALIZATION.getOrDefault(empType.toUpperCase(), empType);
    }

    public List<EligibilityResult> evaluate(EligibilityRequest request) {

        // ── STEP 1: General pre-flight gate (cheapest check, runs first) ──────
        var preflightRequest = new PreflightRequest(request);

        var preflightResult = generalPolicyPreflightService.evaluate(preflightRequest);

        if (!preflightResult.passed()) {
            // FIX BUG-A: record accessor is violations(), not getViolations()
            return List.of(EligibilityResult.rejected(
                    preflightResult.violations(),
                    "Pre-flight gate failed"
            ));
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
                return List.of(EligibilityResult.rejected(
                        List.of(String.format("Service area restricted: PIN %s is outside Indore (452xxx/453xxx)", pin)),
                        "PRYME currently operates only in Indore"
                ));
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
                // ── LOAN AMOUNT RANGE GATE ──────────────────────────────────
                // Products define min_loan_amount / max_loan_amount boundaries.
                // A ₹25L request must NOT surface a product with min ₹35L.
                // This was the root cause of "same products always appear".
                .filter(p -> {
                    BigDecimal reqAmt = request.loanAmount();
                    if (reqAmt == null || reqAmt.compareTo(BigDecimal.ZERO) <= 0) return true; // No amount → don't filter
                    boolean aboveMin = p.getMinLoanAmount() == null || reqAmt.compareTo(p.getMinLoanAmount()) >= 0;
                    boolean belowMax = p.getMaxLoanAmount() == null || reqAmt.compareTo(p.getMaxLoanAmount()) <= 0;
                    if (!aboveMin || !belowMax) {
                        log.info("   ⛔ LOAN_AMOUNT_RANGE: product={} excluded — requested={} not in [{}, {}]",
                                p.getProductCode(), reqAmt, p.getMinLoanAmount(), p.getMaxLoanAmount());
                    }
                    return aboveMin && belowMax;
                })
                .toList();
        log.info("   After loanType='{}' + active + loanAmount filter: {} candidates", normalizedLoanType, candidates.size());

        if (candidates.isEmpty()) {
            log.warn("❌ STEP 2 FAILED: No candidates after filter. loanType='{}' → normalized='{}', cibil={}, lenderId={}",
                    request.loanType(), normalizedLoanType, request.cibilScore(), request.lenderId());
            return List.of(EligibilityResult.rejected(
                    List.of(String.format("No matching products for CIBIL %d and loanType %s",
                            request.cibilScore(), normalizedLoanType)),
                    "No active loan products found for this lender and loan type"
            ));
        }

        // ── STEP 3: Per-product evaluation ───────────────────────────────────
        var results = new ArrayList<EligibilityResult>();

        for (var product : candidates) {
            try {
                results.add(evaluateProduct(product, request));
            } catch (SurrogatePolicyNotFoundException e) {
                log.warn("Surrogate policy missing for product={}: {}",
                        product.getProductCode(), e.getMessage());
                results.add(EligibilityResult.rejected(
                        List.of("Surrogate policy not configured for this product"),
                        "Failed to resolve surrogate income: " + e.getMessage()
                ));
            }
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
        // NORMALIZATION: Frontend sends SELF_EMPLOYED/PROFESSIONAL → DB stores SEP/SENP.
        final String normalizedEmpType = normalizeEmploymentType(
                request.employmentType() != null ? request.employmentType() : null);
        log.info("   STEP 3: empType='{}' → normalized='{}', surrogate='{}', product={}",
                request.employmentType(), normalizedEmpType, applicantProgram, product.getProductCode());

        var conditions = allConditions.stream()
                .filter(c -> c.getSurrogate() == null
                        || c.getSurrogate().isBlank()
                        || (applicantProgram != null
                            && c.getSurrogate().equalsIgnoreCase(applicantProgram)))
                .filter(c -> c.getEmploymentType() == null
                        || c.getEmploymentType().isBlank()
                        || (normalizedEmpType != null
                            && c.getEmploymentType().equalsIgnoreCase(normalizedEmpType)))
                .toList();

        // If no conditions match the applicant's employment type + surrogate,
        // this product doesn't serve this applicant profile at all.
        if (conditions.isEmpty()) {
            log.warn("⏭️ Product {} has no conditions for empType='{}' (normalized='{}'), surrogate='{}'. Skipping. DB conditions had empTypes: {}",
                    product.getProductCode(), request.employmentType(), normalizedEmpType, applicantProgram,
                    allConditions.stream().map(EligibilityCondition::getEmploymentType).distinct().toList());
            return EligibilityResult.ineligible(
                    product.getProductCode(),
                    product.getProductName(),
                    List.of(String.format("No eligibility lane for empType=%s (normalized=%s), surrogate=%s",
                            request.employmentType(), normalizedEmpType, applicantProgram)),
                    "No matching condition lane for applicant profile"
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
        final String rawPropertySubType = request.propertyType() != null ? request.propertyType().toUpperCase() : "RESIDENTIAL";
        applicantPayload.put("propertyCategory", resolvedPropertyCategory);

        // b. Condition checks: age, business vintage, work experience, property,
        //    city, CIBIL floor, ITR requirement + SpEL rules
        //
        //    🧠 CORRECT SEMANTICS: Each condition is an independent eligibility LANE.
        //    The product is eligible if AT LEAST ONE condition passes all checks.
        //    Example: HDFC Home Loan has 4 conditions:
        //      - Salaried NIP (workExp >= 1, FOIR 75%, LTV 65%)
        //      - Self-Employed NIP (bizAge >= 3, FOIR 95%, LTV 45%)
        //      - Self-Employed BANKING (bizAge >= 3, FOIR 55%, LTV 85%)
        //      - Self-Employed GST (bizAge >= 2, FOIR 75%, LTV 65%)
        //    A SALARIED applicant need only pass the first one.
        EligibilityCondition matchedCondition = null;
        List<String> allRejectionReasons = new ArrayList<>();

        for (var c : conditions) {
            List<String> reasonsForThisCondition = new ArrayList<>();

            // ── Standard numeric/range checks ────────
            if (c.getMinAge() != null && request.applicantAge() < c.getMinAge()) {
                reasonsForThisCondition.add(String.format("AGE_TOO_LOW: applicantAge=%d < minAge=%d (condition=%d, surrogate=%s)",
                        request.applicantAge(), c.getMinAge(), c.getId(), c.getSurrogate()));
            }
            if (c.getMaxAge() != null && request.applicantAge() > c.getMaxAge()) {
                reasonsForThisCondition.add(String.format("AGE_TOO_HIGH: applicantAge=%d > maxAge=%d (condition=%d, surrogate=%s)",
                        request.applicantAge(), c.getMaxAge(), c.getId(), c.getSurrogate()));
            }
            if (c.getBusinessAgeYears() != null && request.businessAgeYears() < c.getBusinessAgeYears()) {
                reasonsForThisCondition.add(String.format("BIZ_VINTAGE_LOW: businessAge=%d < required=%d (condition=%d, surrogate=%s)",
                        request.businessAgeYears(), c.getBusinessAgeYears(), c.getId(), c.getSurrogate()));
            }
            if (c.getWorkExpYears() != null && request.workExpYears() < c.getWorkExpYears()) {
                reasonsForThisCondition.add(String.format("WORK_EXP_LOW: workExp=%d < required=%d (condition=%d, surrogate=%s)",
                        request.workExpYears(), c.getWorkExpYears(), c.getId(), c.getSurrogate()));
            }

            // ── PROPERTY TYPE ALLOW-LIST CHECK ────────
            if (c.getPropertyType() != null) {
                String allowedRaw = c.getPropertyType().toUpperCase();
                boolean propertyAllowed = Arrays.stream(allowedRaw.split("[,;]"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .anyMatch(allowed -> allowed.equalsIgnoreCase(resolvedPropertyCategory)
                                || allowed.equalsIgnoreCase(rawPropertySubType));
                if (!propertyAllowed) {
                    reasonsForThisCondition.add(String.format("PROPERTY_NOT_ALLOWED: subType=%s, resolved=%s, allowList='%s' (condition=%d, surrogate=%s)",
                            rawPropertySubType, resolvedPropertyCategory, c.getPropertyType(), c.getId(), c.getSurrogate()));
                }
            }

            if (c.getCityTier() != null && !c.getCityTier().equalsIgnoreCase(request.cityTier())) {
                reasonsForThisCondition.add(String.format("CITY_TIER_MISMATCH: requested=%s, required=%s (condition=%d)",
                        request.cityTier(), c.getCityTier(), c.getId()));
            }
            if (c.getCibilMin() != null && request.cibilScore() < c.getCibilMin()) {
                reasonsForThisCondition.add(String.format("CIBIL_TOO_LOW: score=%d < conditionMin=%d (condition=%d)",
                        request.cibilScore(), c.getCibilMin(), c.getId()));
            }
            if (c.getItrRequiredYears() != null
                    && request.itrYearsAvailable() != null
                    && request.itrYearsAvailable() < c.getItrRequiredYears()) {
                reasonsForThisCondition.add(String.format("ITR_YEARS_LOW: available=%d < required=%d (condition=%d)",
                        request.itrYearsAvailable(), c.getItrRequiredYears(), c.getId()));
            }

            // ── SpEL / deny-list / memo rules ────────
            if (!smartEvaluate(c.getProfileRestrictions(), applicantPayload, "employmentType")) {
                reasonsForThisCondition.add(String.format("PROFILE_RESTRICTED: empType=%s hit deny-list (condition=%d, surrogate=%s)",
                        request.employmentType(), c.getId(), c.getSurrogate()));
            }
            if (!smartEvaluatePropertyDenyList(c.getNegativeProperty(), rawPropertySubType, resolvedPropertyCategory)) {
                reasonsForThisCondition.add(String.format("NEGATIVE_PROPERTY: subType=%s, category=%s denied by '%s' (condition=%d)",
                        rawPropertySubType, resolvedPropertyCategory, c.getNegativeProperty(), c.getId()));
            }
            if (!smartEvaluate(c.getConditions(), applicantPayload, null)) {
                reasonsForThisCondition.add(String.format("CONDITION_RULE_FAILED: rule='%s' (condition=%d)",
                        c.getConditions(), c.getId()));
            }
            if (!smartEvaluate(c.getDeviationFormulae(), applicantPayload, null)) {
                reasonsForThisCondition.add(String.format("DEVIATION_RULE_FAILED: rule='%s' (condition=%d)",
                        c.getDeviationFormulae(), c.getId()));
            }

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
            log.warn("❌ Product {} REJECTED for applicant [cibil={}, empType={}, propType={}, age={}]. All {} condition lanes failed. Reasons: {}",
                    product.getProductCode(), request.cibilScore(), request.employmentType(),
                    request.propertyType(), request.applicantAge(), conditions.size(), allRejectionReasons);

            return EligibilityResult.ineligible(
                    product.getProductCode(),
                    product.getProductName(),
                    allRejectionReasons,
                    "Applicant profile does not satisfy any eligibility lane"
            );
        }

        // c. Resolve effective FOIR from matched condition (static field)
        final BigDecimal effectiveFoir = matchedCondition.getFoirMax() != null
                ? matchedCondition.getFoirMax()
                : (product.getMaxEmiNmiRatio() != null ? product.getMaxEmiNmiRatio() : DEFAULT_FOIR);

        // ── Resolve effective LTV from matched condition (static field)
        final BigDecimal effectiveLtv = matchedCondition.getLtvAllowed() != null
                ? matchedCondition.getLtvAllowed()
                : (product.getLtv() != null ? product.getLtv() : BigDecimal.ZERO);

        // d. Resolve surrogate income (monthly, BigDecimal precision)
        var computedIncome = surrogateIncomeResolver.resolve(request.incomeComputationInput());

        // ── WIRED: minIncome check — enforced against computed income.
        //    Uses the best available income figure: computed surrogate income
        //    first, then declared grossMonthlyIncome if present.
        BigDecimal incomeForFloorCheck = computedIncome;
        if ((incomeForFloorCheck == null || incomeForFloorCheck.compareTo(BigDecimal.ZERO) <= 0)
                && request.grossMonthlyIncome() != null) {
            incomeForFloorCheck = request.grossMonthlyIncome();
        }
        final BigDecimal effectiveIncome = incomeForFloorCheck;
        boolean incomeFloorFailed = matchedCondition.getMinIncome() != null
                && effectiveIncome != null
                && effectiveIncome.compareTo(matchedCondition.getMinIncome()) < 0;
        if (incomeFloorFailed) {
            log.warn("❌ Product {} INCOME_FLOOR_FAILED: income={}, required={} (condition={})",
                    product.getProductCode(), effectiveIncome, matchedCondition.getMinIncome(), matchedCondition.getId());
            return EligibilityResult.ineligible(
                    product.getProductCode(),
                    product.getProductName(),
                    List.of(String.format("Computed income ₹%s below minimum ₹%s", effectiveIncome, matchedCondition.getMinIncome())),
                    "Applicant's resolved monthly income is below the product's minimum income requirement"
            );
        }

        // ── ROI: Resolve dynamically via FinancialComputationEngine ────────────
        ApplicantProfile applicantProfile = new ApplicantProfile(request.cibilScore(), normalizedEmpType, computedIncome);
        final BigDecimal effectiveRoi = financialComputationEngine.resolveRoi(product, applicantProfile, request.loanAmount());

        log.debug("ROI resolved: product={} computedRoi={}",
                product.getProductCode(), effectiveRoi);

        // e. Calculate proposed EMI using closed-form PMT formula
        var proposedEmi = calculateProposedEmiWithRate(request.loanAmount(), effectiveRoi, request.requestedTenureMonths());

        // f. FOIR check
        if (!checkFoir(request.existingEmiTotal(), proposedEmi, computedIncome, effectiveFoir)) {
            return EligibilityResult.ineligible(
                    product.getProductCode(),
                    product.getProductName(),
                    List.of(String.format("FOIR exceeded: effective limit is %.0f%%",
                            effectiveFoir.multiply(BigDecimal.valueOf(100)))),
                    "Total EMI obligations exceed the program FOIR limit"
            );
        }

        // g. Maximum eligible loan amount (income × FOIR − existing EMI)
        var maxEligibleAmount = calculateMaxEligibleAmount(
                computedIncome, request.existingEmiTotal(), effectiveFoir);

        // h. LTV check — USES effectiveLtv (condition-level override)
        boolean ltvDeviated = request.loanAmount()
                .compareTo(request.propertyValue().multiply(effectiveLtv, MathContext.DECIMAL128)) > 0;

        var finalLoanAmount = request.loanAmount()
                .min(request.propertyValue().multiply(effectiveLtv, MathContext.DECIMAL128))
                .min(maxEligibleAmount);

        // ── Processing Fee: static percentage × loan amount ──────────────────
        final BigDecimal processingFee = financialComputationEngine.resolveProcessingFee(
                product, finalLoanAmount);

        log.debug("Processing fee resolved: product={} loanAmount={} → fee={}",
                product.getProductCode(), finalLoanAmount, processingFee);

        // i. Build eligible result — now carries effectiveRoi, effectiveLtv, and processingFee
        return new EligibilityResult(
                true,
                product.getProductCode(),
                product.getProductName(),
                null,
                computedIncome,
                effectiveFoir,
                proposedEmi,
                finalLoanAmount,
                effectiveRoi,
                request.requestedTenureMonths(),
                effectiveLtv,
                ltvDeviated,
                List.of(),
                "Eligible",
                processingFee
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
    //            existing EMI already exceeds income × FOIR.
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal calculateMaxEligibleAmount(BigDecimal computedIncome,
                                                  BigDecimal existingEmiTotal,
                                                  BigDecimal effectiveFoir) {
        BigDecimal maxAllowedEmi = computedIncome.multiply(effectiveFoir, MathContext.DECIMAL128);
        return maxAllowedEmi.subtract(safe(existingEmiTotal)).max(BigDecimal.ZERO);
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
                    "Rule evaluation error for expression: " + expressionString + " -> " + e.getMessage()
            );
        }
    }

    /**
     * "200 IQ" Smart Evaluator: Handles SpEL expressions, implicit SpEL, text deny-lists, and pure text memos.
     * Returns TRUE if the rule PASSES or is just a MEMO. Returns FALSE if the rule FAILS.
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
                log.error("Explicit SpEL execution failed for rule '{}'. Rejecting. Error: {}", ruleText, e.getMessage());
                return false; // Explicit SpEL failure blocks the loan for safety
            }
        }

        // 2. Implicit SpEL (contains #, ==, >, <)
        if (trimmed.contains("#") && (trimmed.contains("==") || trimmed.contains(">") || trimmed.contains("<") || trimmed.contains("!="))) {
            try {
                var expr = spelExpressionCacheService.getOrCompile(trimmed);
                Boolean result = expr.getValue(simpleSandboxEvaluationContext, payload, Boolean.class);
                return Boolean.TRUE.equals(result);
            } catch (Exception e) {
                log.trace("Implicit SpEL parsing failed for '{}', falling back to memo/list evaluation. Error: {}", ruleText, e.getMessage());
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
                .anyMatch(denyItem ->
                        denyItem.equalsIgnoreCase(rawSubType) ||
                        denyItem.equalsIgnoreCase(resolvedCategory));

        if (matchesDenyList) {
            log.debug("Property '{}' (category: '{}') matches negative property deny-list: '{}'",
                    rawSubType, resolvedCategory, denyListText);
            return false; // Denied
        }
        return true; // Passed
    }
}
