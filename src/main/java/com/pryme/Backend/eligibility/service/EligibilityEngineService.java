package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.PreflightRequest;
import com.pryme.Backend.eligibility.dto.PreflightResult;
import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.exception.SurrogatePolicyNotFoundException;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.eligibility.service.GeneralPolicyPreflightService;
import com.pryme.Backend.eligibility.service.SurrogateIncomeResolver;
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

    public List<EligibilityResult> evaluate(EligibilityRequest request) {
        // STEP 1 — Pre-flight
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
            return List.of(new EligibilityResult(
                    false,
                    null,
                    null,
                    null,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    request.requestedTenureMonths(),
                    BigDecimal.ZERO,
                    false,
                    preflightResult.violations(),
                    "Preflight failed"
            ));
        }

        // STEP 2 — Candidate product loading
        var candidates = loanProductRepository.findByMinCibilLessThanEqualAndMaxCibilGreaterThanEqual(
                        request.cibilScore(), request.cibilScore()
                ).stream()
                .filter(p -> p.getLenderId().equals(request.lenderId()) &&
                        p.getLoanType().equalsIgnoreCase(request.loanType()) &&
                        p.isActive())
                .toList();

        if (candidates.isEmpty()) {
            return List.of(new EligibilityResult(
                    false,
                    null,
                    null,
                    null,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    request.requestedTenureMonths(),
                    BigDecimal.ZERO,
                    false,
                    List.of("No matching products"),
                    "No matching loan products found"
            ));
        }

        // STEP 3 — Per-product evaluation loop
        var results = new ArrayList<EligibilityResult>();
        for (var product : candidates) {
            try {
                // a. Load EligibilityCondition
                var conditions = eligibilityConditionRepository.findByProductId(product.getId());

                // Derive effective FOIR exactly once per product evaluation
                BigDecimal effectiveFoir = conditions.stream()
                        .filter(c -> c.getFoirMax() != null)
                        .map(EligibilityCondition::getFoirMax)
                        .findFirst()
                        .orElse(product.getMaxEmiNmiRatio() != null
                                ? product.getMaxEmiNmiRatio()
                                : new BigDecimal("0.65"));

                // b. Check: minAge, maxAge, businessAgeYears, workExpYears, propertyType, cityTier
                boolean conditionsFailed = conditions.stream().anyMatch(c ->
                        (c.getMinAge() != null && request.applicantAge() < c.getMinAge()) ||
                                (c.getMaxAge() != null && request.applicantAge() > c.getMaxAge()) ||
                                (c.getBusinessAgeYears() != null && request.businessAgeYears() < c.getBusinessAgeYears()) ||
                                (c.getWorkExpYears() != null && request.workExpYears() < c.getWorkExpYears()) ||
                                (c.getPropertyType() != null && !c.getPropertyType().contains(request.propertyType())) ||
                                (c.getCityTier() != null && !c.getCityTier().equalsIgnoreCase(request.cityTier()))
                );

                if (conditionsFailed) {
                    results.add(new EligibilityResult(
                            false,
                            product.getProductCode(),
                            product.getProductName(),
                            null,
                            BigDecimal.ZERO,
                            effectiveFoir,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            request.requestedTenureMonths(),
                            BigDecimal.ZERO,
                            false,
                            List.of("Condition check failed"),
                            "One or more eligibility conditions were not met"
                    ));
                    continue;
                }

                // c. Resolve surrogate income
                var computedIncome = surrogateIncomeResolver.resolve(request.incomeComputationInput());

                // d. Calculate proposedEmi via calculators module
                var proposedEmi = calculateProposedEmi(product, request);

                // e. FOIR check: (existingEmiTotal + proposedEmi) / computedIncome > effectiveFoir → rejection
                if (!checkFoir(request.existingEmiTotal(), proposedEmi, computedIncome, effectiveFoir)) {
                    results.add(new EligibilityResult(
                            false,
                            product.getProductCode(),
                            product.getProductName(),
                            null,
                            computedIncome,
                            effectiveFoir,
                            proposedEmi,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            request.requestedTenureMonths(),
                            BigDecimal.ZERO,
                            false,
                            List.of("FOIR check failed"),
                            "The FOIR limit has been exceeded"
                    ));
                    continue;
                }

                // f. Max eligible amount: (computedIncome × effectiveFoir − existingEmiTotal) back-calculated
                var maxEligibleAmount = calculateMaxEligibleAmount(computedIncome, request.existingEmiTotal(), effectiveFoir);

                // g. LTV product-level check: min(requestedAmount, propertyValue×ltv, maxEligible)
                var ltv = product.getLtv();
                var ltvDeviated = request.loanAmount().compareTo(request.propertyValue().multiply(ltv)) > 0;

                var finalLoanAmount = request.loanAmount()
                        .min(request.propertyValue().multiply(ltv, MathContext.DECIMAL128))
                        .min(maxEligibleAmount);

                // h. Build EligibilityResult
                results.add(new EligibilityResult(
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
                        "Eligible for the loan"
                ));
            } catch (SurrogatePolicyNotFoundException e) {
                results.add(new EligibilityResult(
                        false,
                        null,
                        null,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        request.requestedTenureMonths(),
                        BigDecimal.ZERO,
                        false,
                        List.of("Surrogate policy not found"),
                        "Failed to resolve surrogate income"
                ));
            }
        }

        // STEP 4 — Sort: eligible=true first, then roi ascending
        results.sort(Comparator.comparing(EligibilityResult::isEligible).reversed()
                .thenComparing(EligibilityResult::roi));

        // Log: totalCandidates, eligibleCount, topProduct at INFO.
        log.info("Total candidates: {}, Eligible count: {}, Top product: {}",
                candidates.size(),
                results.stream().filter(EligibilityResult::isEligible).count(),
                results.isEmpty() ? "None" : results.get(0).productName());

        return results;
    }

    private BigDecimal calculateProposedEmi(LoanProduct product, EligibilityRequest request) {
        BigDecimal principal = request.loanAmount();
        BigDecimal annualRate = product.getRoi();
        int tenureMonths = request.requestedTenureMonths() > 0 ? request.requestedTenureMonths() : 12;

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
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

    private boolean checkFoir(BigDecimal existingEmiTotal, BigDecimal proposedEmi, BigDecimal computedIncome, BigDecimal effectiveFoir) {
        if (computedIncome == null || computedIncome.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        var totalEmi = existingEmiTotal.add(proposedEmi);
        return totalEmi.divide(computedIncome, 4, RoundingMode.HALF_UP).compareTo(effectiveFoir) <= 0;
    }

    private BigDecimal calculateMaxEligibleAmount(BigDecimal computedIncome, BigDecimal existingEmiTotal, BigDecimal effectiveFoir) {
        return computedIncome.multiply(effectiveFoir, MathContext.DECIMAL128).subtract(existingEmiTotal);
    }

    private BigDecimal calculateRoi(LoanProduct product, EligibilityRequest request) {
        return product.getRoi() != null ? product.getRoi() : BigDecimal.ZERO;
    }
}