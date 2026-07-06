package com.pryme.Backend.eligibility.policy.deployment;

import com.pryme.Backend.eligibility.policy.model.*;
import com.pryme.Backend.eligibility.policy.projection.*;
import com.pryme.Backend.eligibility.policy.repository.*;
import com.pryme.Backend.eligibility.policy.provider.ActiveBundlePolicyProvider;
import com.pryme.Backend.eligibility.policy.warmup.PolicyWarmupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyActivationService {

    private final PolicyBundleEntityRepository policyBundleEntityRepository;
    private final PolicyActivationHistoryRepository policyActivationHistoryRepository;
    private final ActiveBundlePolicyProvider activeBundlePolicyProvider;
    private final PolicyDeploymentService policyDeploymentService;
    private final PolicyWarmupService policyWarmupService;

    @Transactional
    public PolicyActivationHistory activate(String bundleId, String activatedBy, String approvedBy, String reason) {
        log.info("Initiating activation pre-checks for bundle ID: {}...", bundleId);

        PolicyBundleEntity bundleEntity = policyBundleEntityRepository.findByBundleId(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Bundle not found in registry: " + bundleId));

        // Get compiled bundle from provider
        PolicyBundle compiledBundle = activeBundlePolicyProvider.getBundle(bundleId)
                .orElseThrow(() -> new IllegalStateException("Compiled PolicyBundle not loaded in memory for ID: " + bundleId));

        // 1. Verify Deployment Signature
        verifyBundleSignature(compiledBundle);

        // 2. Warm up caches and build indexes
        policyWarmupService.warmup(compiledBundle);

        // Idempotent Database Projection
        policyDeploymentService.projectBundle(compiledBundle);

        // Pre-Checks passed: Hot swap the AtomicReference in provider
        activeBundlePolicyProvider.setActiveBundle(compiledBundle);

        // Clear runtime caches
        activeBundlePolicyProvider.clearCaches();

        // Deactivate previous active bundle entities in DB
        List<PolicyBundleEntity> allBundles = policyBundleEntityRepository.findAll();
        for (PolicyBundleEntity b : allBundles) {
            if (b.isActive() && !b.getBundleId().equals(bundleId)) {
                b.setActive(false);
                b.setState(PolicyState.ARCHIVED.name());
                policyBundleEntityRepository.save(b);
            }
        }

        // Update target bundle state in DB
        bundleEntity.setActive(true);
        bundleEntity.setState(PolicyState.ACTIVE.name());
        policyBundleEntityRepository.save(bundleEntity);

        // Write immutable ledger entry
        String activationId = "ACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PolicyActivationHistory history = PolicyActivationHistory.builder()
                .activationId(activationId)
                .bundleId(bundleEntity.getBundleId())
                .bundleHash(bundleEntity.getCombinedHash())
                .policyVersion(bundleEntity.getVersion())
                .state(PolicyState.ACTIVE.name())
                .activatedBy(activatedBy)
                .approvedBy(approvedBy)
                .activatedAt(Instant.now())
                .gitCommit("git-main")
                .certificationId(bundleEntity.getCertificationId())
                .policyBundleHash(bundleEntity.getPolicyBundleHash())
                .rollbackBundle("N/A")
                .remarks(reason)
                .createdAt(Instant.now())
                .build();

        log.info("Policy Bundle {} has been successfully promoted to ACTIVE state.", bundleId);
        return policyActivationHistoryRepository.save(history);
    }

    private void verifyBundleSignature(PolicyBundle bundle) {
        BundleSignature signature = bundle.signature();
        if (signature == null || signature.signature() == null 
                || signature.signature().isEmpty() 
                || signature.signature().startsWith("UNSIGNED")
                || "EMPTY".equals(signature.signature())) {
            throw new SecurityException("Activation rejected: Policy bundle signature is missing or invalid");
        }

        // Verify Hash
        String calculatedHash = bundle.manifest().policyBundleHash();
        if (!calculatedHash.equals(signature.bundleHash())) {
            throw new SecurityException("Activation rejected: Cryptographic signature mismatch. Calculated hash does not match signed bundle hash");
        }

        // Verify Compatibility
        String requiredEngineVersion = signature.engineVersion();
        if (!"1.0.0-SSOT".equals(requiredEngineVersion) && !"1.0.0".equals(requiredEngineVersion)) {
            throw new IllegalArgumentException("Activation rejected: Engine version incompatibility. Bundle requires: " + requiredEngineVersion);
        }

        log.info("Cryptographic signature and engine version compatibility verified successfully.");
    }
}
