package com.pryme.Backend.recommendation;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class LoanProductSpecifications {

    private LoanProductSpecifications() {
    }

    public static Specification<LoanProduct> salaryEligible(BigDecimal salary) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("minSalary"), salary);
    }

    public static Specification<LoanProduct> cibilEligible(Integer cibil) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("minCibil"), cibil);
    }

    public static Specification<LoanProduct> activeBankOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("bank").get("isActive"));
    }
}
