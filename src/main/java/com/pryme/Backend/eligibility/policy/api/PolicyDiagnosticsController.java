package com.pryme.Backend.eligibility.policy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryme.Backend.eligibility.policy.provider.ActiveBundlePolicyProvider;
import com.pryme.Backend.eligibility.policy.deployment.PolicyActivationService;
import com.pryme.Backend.eligibility.policy.repository.PolicyBundleEntityRepository;
import com.pryme.Backend.eligibility.policy.repository.PolicyActivationHistoryRepository;
import com.pryme.Backend.eligibility.policy.diff.PolicyDiffService;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.model.BundleManifest;
import com.pryme.Backend.eligibility.policy.model.PolicyState;
import com.pryme.Backend.eligibility.policy.projection.PolicyBundleEntity;
import com.pryme.Backend.eligibility.policy.projection.PolicyActivationHistory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class PolicyDiagnosticsController {

    private final ActiveBundlePolicyProvider activeBundlePolicyProvider;
    private final PolicyActivationService policyActivationService;
    private final PolicyBundleEntityRepository policyBundleEntityRepository;
    private final PolicyActivationHistoryRepository policyActivationHistoryRepository;
    private final PolicyDiffService policyDiffService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping("/policy/status")
    public ResponseEntity<Map<String, Object>> getPolicyStatus() {
        PolicyBundle active = activeBundlePolicyProvider.getActiveBundle();
        if (active == null) {
            return ResponseEntity.ok(Map.of("message", "No active policy bundle."));
        }
        BundleManifest manifest = active.manifest();

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("releaseId", "PRYME-" + java.time.LocalDate.now().toString().replace("-", "") + "-001");
        status.put("bundleVersion", manifest.version());
        status.put("state", manifest.state().name());
        status.put("registryFrozen", true);
        status.put("certified", manifest.state() == PolicyState.CERTIFIED || manifest.state() == PolicyState.ACTIVE);
        status.put("engineVersion", "1.0.0-SSOT");
        status.put("gitCommit", manifest.gitCommit());
        status.put("masterDataVersion", "1.0.0");
        status.put("workbookHash", manifest.policyBundleHash());
        status.put("activeSince", manifest.createdTime().toString());
        status.put("activatedBy", active.metadata() != null ? active.metadata().uploadedBy() : "system");
        status.put("health", manifest.active() ? "GREEN" : "YELLOW");

        return ResponseEntity.ok(status);
    }

    @GetMapping("/policy/history")
    public ResponseEntity<List<PolicyActivationHistory>> getPolicyHistory() {
        return ResponseEntity.ok(policyActivationHistoryRepository.findAll());
    }

    @GetMapping("/policy/{bundleId}")
    public ResponseEntity<PolicyBundleDetailsResponse> getPolicyDetails(@PathVariable String bundleId) {
        Optional<PolicyBundleEntity> entityOpt = policyBundleEntityRepository.findByBundleId(bundleId);
        if (entityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PolicyBundleEntity entity = entityOpt.get();
        return ResponseEntity.ok(PolicyBundleDetailsResponse.builder()
                .bundleId(entity.getBundleId())
                .version(entity.getVersion())
                .combinedHash(entity.getCombinedHash())
                .workbookHash(entity.getCombinedHash())
                .state(entity.getState())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .certificationId(entity.getCertificationId())
                .active(entity.isActive())
                .build());
    }

    @GetMapping("/policy/diff/{bundleA}/{bundleB}")
    public ResponseEntity<Map<String, Object>> compareBundles(
            @PathVariable String bundleA,
            @PathVariable String bundleB) {
        Optional<PolicyBundle> aOpt = activeBundlePolicyProvider.getBundle(bundleA);
        Optional<PolicyBundle> bOpt = activeBundlePolicyProvider.getBundle(bundleB);
        if (aOpt.isEmpty() || bOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "One or both bundles not loaded."));
        }

        Map<String, Object> diff = policyDiffService.diff(aOpt.get(), bOpt.get());
        return ResponseEntity.ok(diff);
    }

    @PostMapping("/policy/activate")
    public ResponseEntity<PolicyActivationHistory> activate(
            @RequestParam String bundleId,
            @RequestParam String activatedBy,
            @RequestParam String approvedBy,
            @RequestParam String reason) {
        PolicyActivationHistory history = policyActivationService.activate(bundleId, activatedBy, approvedBy, reason);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/certification/latest")
    public ResponseEntity<Map<String, Object>> getLatestCertification() {
        try {
            File manifestFile = new File("evidence/latest/manifest.json");
            if (manifestFile.exists()) {
                Map<String, Object> manifest = OBJECT_MAPPER.readValue(manifestFile, Map.class);
                return ResponseEntity.ok(manifest);
            }
        } catch (Exception e) {
            // Ignore
        }
        return ResponseEntity.notFound().build();
    }
}
