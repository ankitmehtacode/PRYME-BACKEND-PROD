// File: src/main/java/com/pryme/Backend/eligibility/service/EligibilityEngineService.java

package com.pryme.Backend.eligibility.service;

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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EligibilityEngineService {

    private final GeneralPolicyPreflightService generalPolicyPreflightService;
    private final LoanProductRepository loanProductRepository;
    private final EligibilityConditionRepository eligibilityConditionRepository;
    private final SurrogateIncomeResolver surrogateIncomeResolver;

    private static final BigDecimal DEFAULT_FOIR = new BigDecimal("0.65");

    public List<EligibilityResult> evaluate(EligibilityRequest request) {

        // ── STEP 1: General pre-flight gate (cheapest check, runs first) ──────
        var preflightRequest = new PreflightRequest(
                request.lenderId(),
                request.loanType(),
                request.cibilScore(),
                request.propertyType(),
                request.loanAmount(),
                request.propertyValue(),
                request.requestedTenureMonths(),
                request.monthlyIncome(),
                request.existingEmiTotal()
        );
        var preflightResult = generalPolicyPreflightService.evaluate(preflightRequest);

        if (!preflightResult.passed()) {
            // FIX BUG-A: record accessor is violations(), not getViolations()
            return List.of(EligibilityResult.rejected(
                    preflightResult.violations().stream()
                            .map(v -> v.reason())
                            .toList(),
                    "Pre-flight gate failed"
            ));
        }

        // ── STEP 2: Load candidate products by CIBIL band ────────────────────
        var candidates = loanProductRepository
                .findByMinCibilLessThanEqualAndMaxCibilGreaterThanEqual(
                        request.cibilScore(), request.cibilScore())
                .stream()
                // FIX BUG-B: lenderId is Long — no .getId() call
                // FIX BUG-C: loanType is String — no .name() call
                .filter(p -> p.getLenderId().equals(request.lenderId())
                        && p.getLoanType().equalsIgnoreCase(request.loanType())
                        && p.isActive())
                .toList();

        if (candidates.isEmpty()) {
            return List.of(EligibilityResult.rejected(
                    List.of("No matching products for CIBIL " + request.cibilScore()),
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

    // ─────────────────────────────────────────────────────────────────────────
    // Per-product evaluation — extracted to keep evaluate() readable
    // ─────────────────────────────────────────────────────────────────────────
    private EligibilityResult evaluateProduct(LoanProduct product, EligibilityRequest request) {

        // a. Load eligibility conditions for this product
        var conditions = eligibilityConditionRepository.findByProductId(product.getId());

        // b. Condition checks: age, business vintage, work experience, property type, city tier
        boolean conditionsFailed = conditions.stream().anyMatch(c ->
                (c.getMinAge() != null && request.applicantAge() < c.getMinAge()) ||
                        (c.getMaxAge() != null && request.applicantAge() > c.getMaxAge()) ||
                        (c.getBusinessAgeYears() != null && request.businessAgeYears() < c.getBusinessAgeYears()) ||
                        (c.getWorkExpYears() != null && request.workExpYears() < c.getWorkExpYears()) ||
                        (c.getPropertyType() != null && !c.getPropertyType().contains(request.propertyType())) ||
                        (c.getCityTier() != null && !c.getCityTier().equalsIgnoreCase(request.cityTier()))
        );
        if (conditionsFailed) {
            return EligibilityResult.ineligible(
                    product.getProductCode(),
                    product.getProductName(),
                    List.of("One or more eligibility conditions not met"),
                    "Applicant profile does not satisfy product conditions"
            );
        }

        // c. Resolve effective FOIR — condition-level overrides product-level.
        //    FIX BUG-I: compute once as a local variable, not duplicated 3×
        //    FIX BUG-D: product.getEffectiveFoir() does not exist — derive it here
        final BigDecimal effectiveFoir = conditions.stream()
                .filter(c -> c.getFoirMax() != null)
                .map(EligibilityCondition::getFoirMax)
                .findFirst()
                .orElseGet(() -> product.getMaxEmiNmiRatio() != null
                        ? product.getMaxEmiNmiRatio()
                        : DEFAULT_FOIR);

        // d. Resolve surrogate income (monthly, BigDecimal precision)
        var computedIncome = surrogateIncomeResolver.resolve(request.incomeComputationInput());

        // e. Calculate proposed EMI using closed-form PMT formula
        var proposedEmi = calculateProposedEmi(product, request);

        // f. FOIR check — uses the single effectiveFoir local variable
        if (!checkFoir(request.existingEmiTotal(), proposedEmi, computedIncome, effectiveFoir)) {
            // FIX BUG-E: getProdctCode() typo corrected to getProductCode()
            return EligibilityResult.ineligible(
                    product.getProductCode(),
                    product.getProductName(),
                    List.of(String.format("FOIR exceeded: effective limit is %.0f%%",
                            effectiveFoir.multiply(BigDecimal.valueOf(100)))),
                    "Total EMI obligations exceed the program FOIR limit"
            );
        }

        // g. Maximum eligible loan amount (income × FOIR − existing EMI, back-calculated)
        var maxEligibleAmount = calculateMaxEligibleAmount(
                computedIncome, request.existingEmiTotal(), effectiveFoir);

        // h. LTV check — BigDecimal.min() chain, no double conversion
        //    FIX BUG-F: removed stray )); that caused syntax error
        var ltv = product.getLtv();
        boolean ltvDeviated = request.loanAmount()
                .compareTo(request.propertyValue().multiply(ltv, MathContext.DECIMAL128)) > 0;

        var finalLoanAmount = request.loanAmount()
                .min(request.propertyValue().multiply(ltv, MathContext.DECIMAL128))
                .min(maxEligibleAmount);

        // i. Build eligible result
        return new EligibilityResult(
                true,
                product.getProductCode(),
                product.getProductName(),
                null,
                computedIncome,
                effectiveFoir,
                proposedEmi,
                finalLoanAmount,
                product.getRoi(),
                request.requestedTenureMonths(),
                ltv,
                ltvDeviated,
                List.of(),
                "Eligible"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PMT formula: EMI = P × [r(1+r)^n] / [(1+r)^n − 1]
    // Closed-form O(1) — no amortisation loop.
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal calculateProposedEmi(LoanProduct product, EligibilityRequest request) {
        BigDecimal principal = request.loanAmount();
        BigDecimal annualRate = product.getRoi();
        int tenureMonths = request.requestedTenureMonths() > 0
                ? request.requestedTenureMonths() : 12;

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // Zero-rate edge case: equal instalments
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        MathContext mc = MathContext.DECIMAL128;
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), mc);
        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate, mc).pow(tenureMonths, mc);
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
}