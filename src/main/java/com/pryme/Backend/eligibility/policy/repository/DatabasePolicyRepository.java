package com.pryme.Backend.eligibility.policy.repository;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.projection.PolicyBundleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DatabasePolicyRepository {

    private final PolicyBundleEntityRepository repository;

    public void save(PolicyBundle bundle) {
        PolicyBundleEntity entity = PolicyBundleMapper.toEntity(bundle);
        repository.save(entity);
    }

    public Optional<PolicyBundleEntity> findByBundleId(String bundleId) {
        return repository.findByBundleId(bundleId);
    }

    public List<PolicyBundleEntity> findAll() {
        return repository.findAll();
    }
}
