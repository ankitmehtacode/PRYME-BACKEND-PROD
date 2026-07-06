package com.pryme.Backend.eligibility.policy.repository;

import com.pryme.Backend.eligibility.policy.projection.PolicyBundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PolicyBundleEntityRepository extends JpaRepository<PolicyBundleEntity, Long> {
    Optional<PolicyBundleEntity> findByBundleId(String bundleId);
}
