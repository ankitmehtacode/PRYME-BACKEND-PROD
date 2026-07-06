package com.pryme.Backend.eligibility.policy.repository;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.projection.PolicyBundleEntity;

public class PolicyBundleMapper {
    public static PolicyBundleEntity toEntity(PolicyBundle bundle) {
        if (bundle == null) return null;
        var manifest = bundle.manifest();
        return PolicyBundleEntity.builder()
            .bundleId(manifest.bundleId())
            .version(manifest.version())
            .combinedHash(manifest.policyBundleHash())
            .policyBundleHash(manifest.policyBundleHash())
            .state(manifest.state().name())
            .createdBy(bundle.metadata().uploadedBy())
            .createdAt(manifest.createdTime())
            .certificationId(manifest.certificationId())
            .active(manifest.active())
            .build();
    }
}
