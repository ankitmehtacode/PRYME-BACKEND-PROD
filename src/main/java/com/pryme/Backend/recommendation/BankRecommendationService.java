package com.pryme.Backend.recommendation;

import com.pryme.Backend.loanproduct.LoanProduct;
import com.pryme.Backend.loanproduct.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankRecommendationService {

    private final LoanProductRepository loanProductRepository;

    @Cacheable(cacheNames = "banks:recommendation", key = "#salary.toPlainString() + ':' + #cibil")
    public List<LoanProduct> recommend(BigDecimal salary, Integer cibil) {
        Specification<LoanProduct> spec = Specification
                .where(LoanProductSpecifications.salaryEligible(salary))
                .and(LoanProductSpecifications.cibilEligible(cibil))
                .and(LoanProductSpecifications.activeBankOnly());

        return loanProductRepository.findAll(spec);
    }
}
