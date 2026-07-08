package com.pryme.Backend.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRewardRepository extends JpaRepository<ProductReward, UUID> {
    Optional<ProductReward> findByProductCode(String productCode);
}
