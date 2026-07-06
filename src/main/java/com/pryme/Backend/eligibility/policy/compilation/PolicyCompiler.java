package com.pryme.Backend.eligibility.policy.compilation;

import com.pryme.Backend.eligibility.policy.importing.PolicySourceInput;
import com.pryme.Backend.eligibility.policy.model.*;
import java.time.Instant;
import java.util.UUID;

public class PolicyCompiler {

    public PolicyBundle compile(PolicySourceInput source, String version, String uploadedBy) {
        String bundleId = "BND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        BundleManifest manifest = new BundleManifest(
            bundleId,
            version,
            source.combinedHash(),
            source.individualHashes(),
            "git-main",
            "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            PolicyState.DRAFT,
            false,
            Instant.now()
        );

        BundleMetadata metadata = new BundleMetadata(
            uploadedBy,
            "ComplianceCommittee",
            Instant.now(),
            Instant.now().plus(java.time.Duration.ofDays(365)),
            "Compiled Policy Bundle from input",
            "N/A"
        );

        BundleSignature signature = new BundleSignature(
            source.combinedHash(),
            "MANIFEST_HASH_VAL",
            "git-main",
            "1.0.0-SSOT",
            version,
            "SIG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            Instant.now()
        );

        return new PolicyBundle(
            manifest,
            metadata,
            signature,
            source.eligibilityRules(),
            source.foirRules(),
            source.pfRules(),
            source.loginFeeRules(),
            source.lowLtvRules(),
            source.roiRules()
        );
    }
}
