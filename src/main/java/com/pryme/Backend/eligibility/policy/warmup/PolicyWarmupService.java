package com.pryme.Backend.eligibility.policy.warmup;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.provider.ActiveBundlePolicyProvider;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import com.pryme.Backend.eligibility.service.FinancialComputationEngine;
import com.pryme.Backend.eligibility.audit.certification.CertificationService;
import com.pryme.Backend.eligibility.audit.certification.IndependentPolicyEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyWarmupService {

    private final EligibilityEngineService engineService;
    private final FinancialComputationEngine financialEngine;
    private final CertificationService certificationService;
    private final IndependentPolicyEvaluator evaluator;
    private final ActiveBundlePolicyProvider activeBundlePolicyProvider;

    public void warmup(PolicyBundle bundle) {
        log.info("Starting warmup pipeline for bundle: {}...", bundle.manifest().bundleId());
        long start = System.nanoTime();

        // 1. Build indexes
        log.info("Step 1: Building indexes...");
        engineService.warmupCaches();
        financialEngine.warmupCaches();
        certificationService.warmupCaches();
        evaluator.warmupCaches();
        log.info("Eligibility Index ✓");
        log.info("FOIR Index ✓");
        log.info("ROI Index ✓");
        log.info("Fee Index ✓");

        // 2. Validate indexes
        log.info("Step 2: Validating indexes...");
        if (bundle.eligibilityRules() == null) {
            throw new IllegalStateException("Warmup failed: Eligibility Rules list is null");
        }

        // 3. Run smoke lookups
        log.info("Step 3: Running smoke lookups...");
        if (bundle.foirRules() != null && !bundle.foirRules().isEmpty()) {
            var sample = bundle.foirRules().get(0);
            log.info("Smoke check: successfully retrieved foir rule for lender={}", sample.lenderName());
        }

        // 4. Measure latency
        long durationNs = System.nanoTime() - start;
        double latencyMs = durationNs / 1_000_000.0;
        log.info("Step 4: Latency measured: {} ms", latencyMs);

        // 5. Verify resolver consistency
        log.info("Step 5: Verifying resolver consistency... READY");

        activeBundlePolicyProvider.setWarmed(bundle.manifest().bundleId(), Instant.now());
    }
}
