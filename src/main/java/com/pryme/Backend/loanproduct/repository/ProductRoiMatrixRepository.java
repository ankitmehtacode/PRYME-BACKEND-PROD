package com.pryme.Backend.loanproduct.repository;

import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRoiMatrixRepository extends JpaRepository<ProductRoiMatrix, Long> {
    List<ProductRoiMatrix> findByProductId(Long productId);
}
