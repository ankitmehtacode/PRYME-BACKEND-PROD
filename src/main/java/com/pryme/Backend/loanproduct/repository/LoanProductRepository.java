// File: src/main/java/com/pryme/Backend/loanproduct/repository/LoanProductRepository.java

package com.pryme.Backend.loanproduct.repository;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long>, JpaSpecificationExecutor<LoanProduct> {

    Optional<LoanProduct> findByProductCode(String productCode);

    // Used by EligibilityEngineService: load all products whose CIBIL band
    // covers the applicant's score, then filter by lenderId + loanType in Java.
    // Both arguments receive request.cibilScore() — intentional (range overlap query).
    List<LoanProduct> findByMinCibilLessThanEqualAndMaxCibilGreaterThanEqual(
            int cibil1, int cibil2);

    List<LoanProduct> findByLenderIdAndLoanTypeAndActive(
            Long lenderId, String loanType, boolean active);

    List<LoanProduct> findByLoanTypeAndActive(String loanType, boolean active);

    List<LoanProduct> findByLoanTypeInAndActive(List<String> loanTypes, boolean active);

    List<LoanProduct> findTop3ByActiveTrueOrderByRoiAsc();
}
