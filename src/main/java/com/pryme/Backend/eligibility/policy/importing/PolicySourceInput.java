package com.pryme.Backend.eligibility.policy.importing;

import com.pryme.Backend.eligibility.policy.model.*;
import java.util.List;
import java.util.Map;

public record PolicySourceInput(
    List<EligibilityPolicyRule> eligibilityRules,
    List<FoirPolicyRule> foirRules,
    List<ProcessingFeeRule> pfRules,
    List<LoginFeeRule> loginFeeRules,
    List<LowLtvRule> lowLtvRules,
    List<ProductRoiMatrixRule> roiRules,
    String combinedHash,
    Map<String, String> individualHashes
) {}
