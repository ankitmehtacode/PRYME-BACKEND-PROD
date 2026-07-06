package com.pryme.Backend.eligibility.policy.provider;

import com.pryme.Backend.eligibility.policy.model.*;
import com.pryme.Backend.eligibility.policy.repository.MemoryPolicyRepository;
import com.pryme.Backend.eligibility.policy.event.PolicyCachesClearedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ActiveBundlePolicyProvider implements PolicyProvider {

    private final AtomicReference<PolicyBundle> activeBundleRef = new AtomicReference<>();
    private final MemoryPolicyRepository memoryPolicyRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicReference<Instant> activatedAtRef = new AtomicReference<>(Instant.now());
    private final AtomicReference<Instant> warmedAtRef = new AtomicReference<>();
    private final AtomicReference<Boolean> cacheWarmRef = new AtomicReference<>(false);

    public ActiveBundlePolicyProvider(MemoryPolicyRepository memoryPolicyRepository, ApplicationEventPublisher eventPublisher) {
        this.memoryPolicyRepository = memoryPolicyRepository;
        this.eventPublisher = eventPublisher;
    }

    public void registerBundle(PolicyBundle bundle) {
        memoryPolicyRepository.save(bundle);
    }

    public void setActiveBundle(PolicyBundle bundle) {
        registerBundle(bundle);
        activeBundleRef.set(bundle);
        activatedAtRef.set(Instant.now());
        warmedAtRef.set(null);
        cacheWarmRef.set(false);
    }

    public void clearActiveBundle() {
        activeBundleRef.set(null);
        activatedAtRef.set(Instant.now());
        warmedAtRef.set(null);
        cacheWarmRef.set(false);
    }

    @Override
    public PolicyBundle getActiveBundle() {
        PolicyBundle active = activeBundleRef.get();
        if (active == null) {
            BundleManifest emptyManifest = new BundleManifest(
                "BASE", "1.0.0", "EMPTY", Map.of(), "N/A", "N/A", PolicyState.DRAFT, false, Instant.now()
            );
            BundleMetadata metadata = new BundleMetadata("SYSTEM", "N/A", Instant.now(), Instant.now(), "EMPTY BUNDLE", "");
            BundleSignature signature = new BundleSignature("EMPTY", "EMPTY", "N/A", "1.0.0-SSOT", "1.0.0", "N/A", Instant.now());
            return new PolicyBundle(emptyManifest, metadata, signature, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }
        return active;
    }

    @Override
    public Optional<PolicyBundle> getBundle(String bundleId) {
        return memoryPolicyRepository.load(bundleId);
    }

    @Override
    public boolean exists(String bundleId) {
        return memoryPolicyRepository.load(bundleId).isPresent();
    }

    @Override
    public String getActiveBundleId() {
        PolicyBundle active = activeBundleRef.get();
        return active != null ? active.manifest().bundleId() : "BASE";
    }

    @Override
    public RuntimeBundleStatus getRuntimeStatus() {
        PolicyBundle active = activeBundleRef.get();
        String bundleId = active != null ? active.manifest().bundleId() : "BASE";
        boolean cacheWarm = cacheWarmRef.get();
        Instant activatedAt = activatedAtRef.get();
        Instant warmedAt = warmedAtRef.get();

        HealthStatus status = HealthStatus.RED;
        if (active != null) {
            status = cacheWarm ? HealthStatus.GREEN : HealthStatus.YELLOW;
        }

        long policyCount = 0;
        if (active != null) {
            if (active.eligibilityRules() != null) policyCount += active.eligibilityRules().size();
            if (active.foirRules() != null) policyCount += active.foirRules().size();
            if (active.pfRules() != null) policyCount += active.pfRules().size();
            if (active.loginFeeRules() != null) policyCount += active.loginFeeRules().size();
            if (active.lowLtvRules() != null) policyCount += active.lowLtvRules().size();
            if (active.roiRules() != null) policyCount += active.roiRules().size();
        }

        Duration age = Duration.between(activatedAt, Instant.now());

        return new RuntimeBundleStatus(
            bundleId,
            cacheWarm,
            activatedAt,
            warmedAt,
            status,
            policyCount,
            age
        );
    }

    @Override
    public void setWarmed(String bundleId, Instant warmedAt) {
        String activeId = getActiveBundleId();
        if (activeId.equals(bundleId)) {
            cacheWarmRef.set(true);
            warmedAtRef.set(warmedAt);
        }
    }

    @Override
    public void clearCaches() {
        eventPublisher.publishEvent(new PolicyCachesClearedEvent(this));
    }
}
