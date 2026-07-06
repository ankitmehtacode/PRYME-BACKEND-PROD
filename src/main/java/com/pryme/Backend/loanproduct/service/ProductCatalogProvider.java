package com.pryme.Backend.loanproduct.service;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.provider.ActiveBundlePolicyProvider;
import com.pryme.Backend.loanproduct.dto.ProductCatalogSnapshot;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 📦 Serves immutable product snapshots decoupled from raw repositories.
 */
@Component
@RequiredArgsConstructor
public class ProductCatalogProvider {

    private final LoanProductRepository loanProductRepository;
    private final ActiveBundlePolicyProvider activeBundlePolicyProvider;

    public ProductCatalogSnapshot getCatalogSnapshot() {
        PolicyBundle bundle = activeBundlePolicyProvider.getActiveBundle();
        String bundleId = bundle != null && bundle.manifest() != null ? bundle.manifest().bundleId() : "BASE";
        String hash = bundle != null && bundle.manifest() != null ? bundle.manifest().policyBundleHash() : "N/A";

        List<LoanProduct> activeProducts = loanProductRepository.findAll().stream()
                .filter(LoanProduct::isActive)
                .toList();

        return new ProductCatalogSnapshot(
                bundleId,
                hash,
                Instant.now(),
                activeProducts,
                activeProducts.size()
        );
    }
}
