package com.pryme.Backend.eligibility.policy.provider;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.model.RuntimeBundleStatus;
import java.time.Instant;
import java.util.Optional;

public interface PolicyProvider {
    PolicyBundle getActiveBundle();
    Optional<PolicyBundle> getBundle(String bundleId);
    boolean exists(String bundleId);
    String getActiveBundleId();
    RuntimeBundleStatus getRuntimeStatus();
    void setWarmed(String bundleId, Instant warmedAt);
    void clearCaches();
}

