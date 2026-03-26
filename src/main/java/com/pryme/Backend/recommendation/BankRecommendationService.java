package com.pryme.Backend.recommendation;

import com.pryme.Backend.loanproduct.LoanProduct;
import com.pryme.Backend.loanproduct.LoanProductRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(BankRecommendationService.class);
    private final LoanProductRepository loanProductRepository;

    @Value("${app.recommendation.max-results:8}")
    private int maxResults;

    // 🧠 160 IQ FIX 1: CACHE COHERENCY
    // stripTrailingZeros() forces "50000.00" and "50000" to evaluate to the exact same cache key.
    @Cacheable(cacheNames = "banks:recommendation",
            key = "#salary != null ? #salary.stripTrailingZeros().toPlainString() + ':' + #cibil : 'null'")
    public List<LoanProduct> recommend(BigDecimal salary, Integer cibil) {

        if (salary == null || cibil == null) {
            log.warn("Recommendation matrix aborted: Incomplete financial footprint.");
            return List.of();
        }

        // 🧠 160 IQ FIX 2: THE ANTI-N+1 FETCH SHIELD
        // Forces Hibernate to pull the attached Bank in the exact same single query.
        // Prevents the Comparator from triggering hundreds of secondary SELECT statements.
        Specification<LoanProduct> fetchRelations = (root, query, cb) -> {
            if (Long.class != query.getResultType()) { // Prevents crash if Spring attempts a count() query
                root.fetch("bank", JoinType.INNER);
            }
            return cb.conjunction();
        };

        Specification<LoanProduct> spec = Specification
                .where(fetchRelations)
                .and(LoanProductSpecifications.salaryEligible(salary))
                .and(LoanProductSpecifications.cibilEligible(cibil))
                .and(LoanProductSpecifications.activeBankOnly());

        // 🧠 DEFENSIVE SORTING MATRIX
        Comparator<LoanProduct> ranking = Comparator
                .comparing((LoanProduct p) -> fitScore(p, salary, cibil), Comparator.reverseOrder())
                .thenComparing(p -> safe(p.getInterestRate()))
                .thenComparing(p -> safe(p.getProcessingFee()))
                .thenComparing(p -> p.getBank() != null && p.getBank().getBankName() != null
                        ? p.getBank().getBankName() : "UNKNOWN");

        // Note on In-Memory Sorting: Because the active FinTech product catalog is bounded
        // (usually < 1000 items), computing this algorithm in RAM is O(1) instantaneous
        // and keeps the SQL database completely unburdened from heavy CPU math.
        try (var scope = new java.util.concurrent.StructuredTaskScope.ShutdownOnFailure()) {
            var loanProductsFuture = scope.fork(() -> loanProductRepository.findAll(spec));

            scope.join();
            scope.throwIfFailed();

            return loanProductsFuture.resultNow()
                    .stream()
                    .sorted(ranking)
                    .limit(Math.max(1, maxResults))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch loan products", e);
        }
    }

    public BigDecimal fitScore(LoanProduct product, BigDecimal salary, Integer cibil) {
        // 🧠 160 IQ FIX 3: NULL-SAFE MATHEMATICS
        // Protects the JVM thread from crashing if a database row is missing attributes.
        BigDecimal minSalary = safe(product.getMinSalary());
        int minCibil = product.getMinCibil() != null ? product.getMinCibil() : 300;
        BigDecimal interestRate = safe(product.getInterestRate());
        BigDecimal fee = safe(product.getProcessingFee());

        BigDecimal salaryHeadroom = salary.subtract(minSalary).max(BigDecimal.ZERO);
        BigDecimal salaryRatio = salaryHeadroom
                .divide(minSalary.max(BigDecimal.ONE), 6, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        int cibilHeadroomRaw = Math.max(0, cibil - minCibil);
        BigDecimal cibilRatio = BigDecimal.valueOf(cibilHeadroomRaw)
                .divide(BigDecimal.valueOf(300), 6, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        BigDecimal normalizedRate = BigDecimal.ONE
                .subtract(interestRate.divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_UP))
                .max(BigDecimal.ZERO);

        BigDecimal normalizedFee = BigDecimal.ONE
                .subtract(fee.divide(BigDecimal.valueOf(6), 6, RoundingMode.HALF_UP))
                .max(BigDecimal.ZERO);

        return salaryRatio.multiply(BigDecimal.valueOf(0.30))
                .add(cibilRatio.multiply(BigDecimal.valueOf(0.30)))
                .add(normalizedRate.multiply(BigDecimal.valueOf(0.30)))
                .add(normalizedFee.multiply(BigDecimal.valueOf(0.10)))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
