package com.pryme.Backend.eligibility.policy.repository;

import com.pryme.Backend.eligibility.policy.projection.PolicyActivationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyActivationHistoryRepository extends JpaRepository<PolicyActivationHistory, Long> {
}
