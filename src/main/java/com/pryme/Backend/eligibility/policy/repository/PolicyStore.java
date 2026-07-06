package com.pryme.Backend.eligibility.policy.repository;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import java.util.Optional;
import java.util.List;

public interface PolicyStore {
    void save(PolicyBundle bundle);
    Optional<PolicyBundle> load(String bundleId);
    void delete(String bundleId);
    List<PolicyBundle> history();
}
