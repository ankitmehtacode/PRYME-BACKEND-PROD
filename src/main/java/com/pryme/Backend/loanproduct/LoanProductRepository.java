package com.pryme.Backend.loanproduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID>, JpaSpecificationExecutor<LoanProduct> {
    java.util.List<LoanProduct> findTop3ByBankIsActiveTrueOrderByInterestRateAsc();
    java.util.List<LoanProduct> findAllByTypeAndBankIsActiveTrue(LoanProductType type);
}


