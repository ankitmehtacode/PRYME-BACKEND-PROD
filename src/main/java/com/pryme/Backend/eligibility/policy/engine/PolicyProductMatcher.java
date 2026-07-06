package com.pryme.Backend.eligibility.policy.engine;

import com.pryme.Backend.eligibility.policy.model.EmploymentType;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.eligibility.service.EmploymentCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 🎯 Uniquely resolves lender details, employment profiles, and product prefixes
 * into exactly one active LoanProduct.
 */
@Component
@RequiredArgsConstructor
public class PolicyProductMatcher {

    private final EmploymentCompatibilityService compatibilityService;
    private final com.pryme.Backend.eligibility.service.CentralizedNormalizer normalizer;

    public Optional<LoanProduct> matchOptional(
            Collection<LoanProduct> products,
            String lenderName,
            String loanType,
            EmploymentType empType,
            String productCodePrefix
    ) {
        if (products == null) {
            return Optional.empty();
        }

        List<LoanProduct> matched = products.stream()
                .filter(LoanProduct::isActive)
                .filter(p -> {
                    String normP = normalizer.normalizeLender(p.getLenderName());
                    String normL = normalizer.normalizeLender(lenderName);
                    return normP.equalsIgnoreCase(normL);
                })
                .filter(p -> productCodePrefix == null || productCodePrefix.isEmpty() 
                        || (p.getProductCode() != null && p.getProductCode().toUpperCase().startsWith(productCodePrefix.toUpperCase())))
                .filter(p -> p.getLoanType().equalsIgnoreCase(loanType))
                .filter(p -> compatibilityService.isProductAllowedForEmploymentType(p.getProductCode(), p.getLenderName(), empType))
                .toList();

        if (matched.isEmpty()) {
            System.out.println("[DIAGNOSTIC] No product match for lender=" + lenderName + ", loanType=" + loanType + ", empType=" + empType + ", prefix=" + productCodePrefix);
            for (LoanProduct p : products) {
                if (p.isActive()) {
                    String normP = normalizer.normalizeLender(p.getLenderName());
                    String normL = normalizer.normalizeLender(lenderName);
                    boolean lenderMatch = normP.equalsIgnoreCase(normL);
                    if (lenderMatch) {
                        boolean prefixMatch = productCodePrefix == null || productCodePrefix.isEmpty() 
                                || (p.getProductCode() != null && p.getProductCode().toUpperCase().startsWith(productCodePrefix.toUpperCase()));
                        boolean typeMatch = p.getLoanType().equalsIgnoreCase(loanType);
                        boolean compatMatch = compatibilityService.isProductAllowedForEmploymentType(p.getProductCode(), p.getLenderName(), empType);
                        System.out.println("  Candidate: code=" + p.getProductCode() + ", lender=" + p.getLenderName() + 
                                ", type=" + p.getLoanType() + ", lenderMatch=" + lenderMatch + ", prefixMatch=" + prefixMatch + 
                                ", typeMatch=" + typeMatch + ", compatMatch=" + compatMatch);
                    }
                }
            }
            return Optional.empty();
        }

        if (matched.size() > 1) {
            // Try exact code matching to resolve duplicates if possible
            List<LoanProduct> exactMatch = matched.stream()
                    .filter(p -> p.getProductCode().equalsIgnoreCase(productCodePrefix))
                    .toList();
            if (exactMatch.size() == 1) {
                return Optional.of(exactMatch.get(0));
            }

            // Resolving Bajaj duplicates: BAJAJ-HL-0002 is preferred over BAJAJ-HL-0003
            List<LoanProduct> preferredMatch = matched.stream()
                    .filter(p -> !p.getProductCode().endsWith("-0003") && (p.getProductName() == null || !p.getProductName().contains("Industry Margin")))
                    .toList();
            if (preferredMatch.size() == 1) {
                return Optional.of(preferredMatch.get(0));
            }

            throw new AmbiguousProductException(String.format(
                    "AMBIGUOUS_PRODUCT_MATCH: Multiple products matched prefix=%s, lender=%s, loanType=%s, empType=%s: %s",
                    productCodePrefix, lenderName, loanType, empType,
                    matched.stream().map(LoanProduct::getProductCode).toList()
            ));
        }

        return Optional.of(matched.get(0));
    }

    public LoanProduct matchUnique(
            Collection<LoanProduct> products,
            String lenderName,
            String loanType,
            EmploymentType empType,
            String productCodePrefix
    ) {
        return matchOptional(products, lenderName, loanType, empType, productCodePrefix)
                .orElseThrow(() -> new ProductNotFoundException(String.format(
                        "PRODUCT_NOT_FOUND: No active product matches lender=%s, loanType=%s, empType=%s, prefix=%s",
                        lenderName, loanType, empType, productCodePrefix
                )));
    }
}
