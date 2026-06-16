package com.pryme.Backend.loanproduct.repository;

import com.pryme.Backend.loanproduct.entity.ProductLoginFeeMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLoginFeeMatrixRepository extends JpaRepository<ProductLoginFeeMatrix, Long> {
    List<ProductLoginFeeMatrix> findByProductId(Long productId);
}
