package com.pryme.Backend.eligibility.policy.model;

import java.util.List;

public record PolicyBundle(
    BundleManifest manifest,
    BundleMetadata metadata,
    BundleSignature signature,
    List<EligibilityPolicyRule> eligibilityRules,
    List<FoirPolicyRule> foirRules,
    List<ProcessingFeeRule> pfRules,
    List<LoginFeeRule> loginFeeRules,
    List<LowLtvRule> lowLtvRules,
    List<ProductRoiMatrixRule> roiRules
) {}
