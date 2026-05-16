// File: src/main/java/com/pryme/Backend/eligibility/repository/EligibilityConditionRepository.java

package com.pryme.Backend.eligibility.repository;

import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EligibilityConditionRepository extends JpaRepository<EligibilityCondition, Long> {

    List<EligibilityCondition> findByProductId(Long productId);

    Optional<EligibilityCondition> findByProductCode(String productCode);

    List<EligibilityCondition> findByProductIdAndActive(Long productId, boolean active);

    Page<EligibilityCondition> findByActive(boolean active, Pageable pageable);
}