package com.pryme.Backend.loanproduct.repository;

import com.pryme.Backend.loanproduct.entity.ProductPfMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPfMatrixRepository extends JpaRepository<ProductPfMatrix, Long> {
    List<ProductPfMatrix> findByProductId(Long productId);
}
