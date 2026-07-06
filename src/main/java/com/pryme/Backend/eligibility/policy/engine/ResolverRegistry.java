package com.pryme.Backend.eligibility.policy.engine;

import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import org.springframework.stereotype.Component;

@Component
public class ResolverRegistry {
    private final EligibilityResolver eligibilityResolver = new EligibilityResolver();
    private final FoirResolver foirResolver;
    private final FeeResolver feeResolver = new FeeResolver();
    private final RoiResolver roiResolver = new RoiResolver();
    private final LowLtvResolver lowLtvResolver = new LowLtvResolver();

    public ResolverRegistry() {
        this.foirResolver = new FoirResolver(new CentralizedNormalizer());
    }

    public ResolverRegistry(CentralizedNormalizer normalizer) {
        this.foirResolver = new FoirResolver(normalizer);
    }

    public EligibilityResolver getEligibilityResolver() {
        return eligibilityResolver;
    }

    public FoirResolver getFoirResolver() {
        return foirResolver;
    }

    public FeeResolver getFeeResolver() {
        return feeResolver;
    }

    public RoiResolver getRoiResolver() {
        return roiResolver;
    }

    public LowLtvResolver getLowLtvResolver() {
        return lowLtvResolver;
    }
}

