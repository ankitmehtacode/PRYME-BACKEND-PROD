package com.pryme.Backend.eligibility.policy.repository;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemoryPolicyRepository implements PolicyStore {

    private final Map<String, PolicyBundle> store = new ConcurrentHashMap<>();

    @Override
    public void save(PolicyBundle bundle) {
        if (bundle != null && bundle.manifest() != null) {
            store.put(bundle.manifest().bundleId(), bundle);
        }
    }

    @Override
    public Optional<PolicyBundle> load(String bundleId) {
        return Optional.ofNullable(store.get(bundleId));
    }

    @Override
    public void delete(String bundleId) {
        store.remove(bundleId);
    }

    @Override
    public List<PolicyBundle> history() {
        return new ArrayList<>(store.values());
    }
}
