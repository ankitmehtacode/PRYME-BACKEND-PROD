package com.pryme.Backend.recommendation;

import com.pryme.Backend.loanproduct.LoanProduct;
import com.pryme.Backend.loanproduct.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankRecommendationService {

    private final LoanProductRepository loanProductRepository;

    @Value("${app.recommendation.max-results:8}")
    private int maxResults;

    @Cacheable(cacheNames = "banks:recommendation", key = "#salary.toPlainString() + ':' + #cibil")
    public List<LoanProduct> recommend(BigDecimal salary, Integer cibil) {
        Specification<LoanProduct> spec = Specification
                .where(LoanProductSpecifications.salaryEligible(salary))
                .and(LoanProductSpecifications.cibilEligible(cibil))
                .and(LoanProductSpecifications.activeBankOnly());

        Comparator<LoanProduct> ranking = Comparator
                .comparing((LoanProduct p) -> fitScore(p, salary, cibil), Comparator.reverseOrder())
                .thenComparing(LoanProduct::getInterestRate)
                .thenComparing(LoanProduct::getProcessingFee)
                .thenComparing(p -> p.getBank().getBankName());

        return loanProductRepository.findAll(spec).stream()
                .sorted(ranking)
                .limit(Math.max(1, maxResults))
                .toList();
    }

    public BigDecimal fitScore(LoanProduct product, BigDecimal salary, Integer cibil) {
        BigDecimal salaryHeadroom = salary.subtract(product.getMinSalary()).max(BigDecimal.ZERO);
        BigDecimal salaryRatio = salaryHeadroom
                .divide(product.getMinSalary().max(BigDecimal.ONE), 6, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        int cibilHeadroomRaw = Math.max(0, cibil - product.getMinCibil());
        BigDecimal cibilRatio = BigDecimal.valueOf(cibilHeadroomRaw)
                .divide(BigDecimal.valueOf(300), 6, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        BigDecimal normalizedRate = BigDecimal.ONE
                .subtract(product.getInterestRate().divide(BigDecimal.valueOf(30), 6, RoundingMode.HALF_UP))
                .max(BigDecimal.ZERO);

        BigDecimal normalizedFee = BigDecimal.ONE
                .subtract(product.getProcessingFee().divide(BigDecimal.valueOf(6), 6, RoundingMode.HALF_UP))
                .max(BigDecimal.ZERO);

        return salaryRatio.multiply(BigDecimal.valueOf(0.30))
                .add(cibilRatio.multiply(BigDecimal.valueOf(0.30)))
                .add(normalizedRate.multiply(BigDecimal.valueOf(0.30)))
                .add(normalizedFee.multiply(BigDecimal.valueOf(0.10)))
                .setScale(4, RoundingMode.HALF_UP);
    }
}
