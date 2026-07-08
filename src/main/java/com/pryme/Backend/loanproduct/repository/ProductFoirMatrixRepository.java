package com.pryme.Backend.loanproduct.repository;

import com.pryme.Backend.loanproduct.entity.ProductFoirMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFoirMatrixRepository extends JpaRepository<ProductFoirMatrix, Long> {
    List<ProductFoirMatrix> findByProductId(Long productId);
}
