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
        var preflightRequest = new PreflightRequest(request);
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
                    preflightResult.getViolations(),
                    "Preflight failed"
            ));
        }

        // STEP 2 — Candidate product loading
        var candidates = loanProductRepository.findByMinCibilLessThanEqualAndMaxCibilGreaterThanEqual(
                request.cibilScore(), request.cibilScore()
        ).stream()
                .filter(p -> p.getBank().getId().equals(request.lenderId()) &&
                        p.getType().name().equalsIgnoreCase(request.loanType()) &&
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
                var conditions = eligibilityConditionRepository.findByLoanProductId(product.getId());

                // b. Check: minAge, maxAge, businessAgeYears, workExpYears, propertyType, cityTier
                if (!conditions.stream().allMatch(c -> c.satisfies(request))) {
                    results.add(new EligibilityResult(
                            false,
                            product.getCode(),
                            product.getName(),
                            null,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
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
                if (!checkFoir(request.existingEmiTotal(), proposedEmi, computedIncome, product.getEffectiveFoir())) {
                    results.add(new EligibilityResult(
                            false,
                            product.getCode(),
                            product.getName(),
                            null,
                            computedIncome,
                            product.getEffectiveFoir(),
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
                var maxEligibleAmount = calculateMaxEligibleAmount(computedIncome, request.existingEmiTotal(), product.getEffectiveFoir());

                // g. LTV product-level check: min(requestedAmount, propertyValue×ltv, maxEligible)
                var ltv = product.getLtv();
                var ltvDeviated = false;
                if (request.loanAmount().compareTo(request.propertyValue().multiply(ltv)) > 0) {
                    ltvDeviated = true;
                }
                var finalLoanAmount = BigDecimal.valueOf(Math.min(
                        request.loanAmount().doubleValue(),
                        Math.min(request.propertyValue().multiply(ltv).doubleValue(), maxEligibleAmount.doubleValue())
                ));

                // h. Build EligibilityResult
                results.add(new EligibilityResult(
                        true,
                        product.getCode(),
                        product.getName(),
                        null,
                        computedIncome,
                        product.getEffectiveFoir(),
                        proposedEmi,
                        finalLoanAmount,
                        calculateRoi(product, request),
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
        // Placeholder for actual calculation logic
        return BigDecimal.ZERO;
    }

    private boolean checkFoir(BigDecimal existingEmiTotal, BigDecimal proposedEmi, BigDecimal computedIncome, BigDecimal effectiveFoir) {
        var totalEmi = existingEmiTotal.add(proposedEmi);
        return totalEmi.divide(computedIncome, 4, java.math.RoundingMode.HALF_UP).compareTo(effectiveFoir) <= 0;
    }

    private BigDecimal calculateMaxEligibleAmount(BigDecimal computedIncome, BigDecimal existingEmiTotal, BigDecimal effectiveFoir) {
        return computedIncome.multiply(effectiveFoir, java.math.MathContext.DECIMAL128).subtract(existingEmiTotal);
    }

    private BigDecimal calculateRoi(LoanProduct product, EligibilityRequest request) {
        // Placeholder for actual ROI calculation logic
        return BigDecimal.ZERO;
    }
}
