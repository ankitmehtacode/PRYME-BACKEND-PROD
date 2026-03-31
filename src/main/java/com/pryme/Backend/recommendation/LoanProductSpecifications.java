package com.pryme.Backend.recommendation;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import org.springframework.data.jpa.domain.Specification;


public final class LoanProductSpecifications {

    private LoanProductSpecifications() {
    }

    public static Specification<LoanProduct> cibilEligible(Integer cibil) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("minCibil"), cibil);
    }

    public static Specification<LoanProduct> activeOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
}
