package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.dto.ApplicantProfile;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import com.pryme.Backend.eligibility.policy.provider.ActiveBundlePolicyProvider;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 FINANCIAL COMPUTATION ENGINE — DYNAMIC RESOLVER
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Resolves processing fees and interest rates (ROI) at runtime using active PolicyBundle in memory.
 * Fallback to base product parameters.
 *
 * @since 2026-06-12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialComputationEngine {

    private final ProductRoiMatrixRepository roiMatrixRepository;
    private final ActiveBundlePolicyProvider activeBundlePolicyProvider;
    private final CentralizedNormalizer centralizedNormalizer;
    private final java.util.Map<String, List<ProductRoiMatrix>> roiCache = new java.util.concurrent.ConcurrentHashMap<>();

    @org.springframework.context.event.EventListener
    public void handleCachesCleared(com.pryme.Backend.eligibility.policy.event.PolicyCachesClearedEvent event) {
        clearCaches();
    }

    public void clearCaches() {
        roiCache.clear();
    }

    public void warmupCaches() {
        clearCaches();
        List<ProductRoiMatrix> all = roiMatrixRepository.findAll();
        for (var r : all) {
            String activeKey = r.getProductId() + ":" + r.getBundleId();
            roiCache.computeIfAbsent(activeKey, k -> new java.util.ArrayList<>()).add(r);

            // Also store under product ID for absolute fallback
            String fallbackKey = r.getProductId() + ":ALL";
            roiCache.computeIfAbsent(fallbackKey, k -> new java.util.ArrayList<>()).add(r);
        }
    }

    /** Scale for all INR fee outputs. */
    private static final int FEE_SCALE = 2;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the absolute processing fee (in ₹) for a given product, loan amount, and employment type.
     */
    public BigDecimal resolveProcessingFee(LoanProduct product, BigDecimal loanAmount, String employmentType) {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "loanAmount must be a positive value; received: " + loanAmount);
        }

        var active = activeBundlePolicyProvider.getActiveBundle();
        if (active != null && active.pfRules() != null && employmentType != null) {
            for (var row : active.pfRules()) {
                if (product.getProductCode() != null && product.getProductCode().equalsIgnoreCase(row.productName())) {
                    // 1. Check Employment Type
                    String rowEmpType = row.employmentType();
                    if (rowEmpType != null) {
                        if (!centralizedNormalizer.matchRoiEmploymentType(rowEmpType, employmentType, product.getLenderName())) {
                            continue;
                        }
                    }

                    // 2. Check Loan Amount Slabs
                    BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                    if (loanAmount.compareTo(minAmt) < 0 || loanAmount.compareTo(maxAmt) > 0) {
                        continue;
                    }

                    // Found matching slab!
                    BigDecimal feeVal = row.pf();
                    BigDecimal baseFee;
                    if (feeVal.compareTo(BigDecimal.ONE) < 0) {
                        baseFee = loanAmount.multiply(feeVal);
                    } else {
                        baseFee = feeVal;
                    }

                    // Apply Tax Rate (total = baseFee * (1 + taxRate))
                    BigDecimal taxRate = row.tax() != null ? row.tax() : new BigDecimal("0.1800");
                    BigDecimal multiplier = BigDecimal.ONE.add(taxRate);
                    BigDecimal totalFee = baseFee.multiply(multiplier);

                    log.debug("Dynamic PF resolved: product={} loanAmount={} baseFee={} taxRate={} → totalFee={}",
                            product.getProductCode(), loanAmount, baseFee, taxRate, totalFee);

                    return totalFee.setScale(FEE_SCALE, RoundingMode.HALF_UP);
                }
            }
        }

        // Fallback to static percentage: loanAmount × staticRate (tax-exclusive)
        if (product.getProcessingFee() != null) {
            return loanAmount.multiply(product.getProcessingFee())
                    .setScale(FEE_SCALE, RoundingMode.HALF_UP);
        }

        // No fee configured
        log.debug("No static/dynamic fee for product={}. Returning ZERO.",
                product.getProductCode());
        return BigDecimal.ZERO;
    }

    /**
     * Overloaded method for backward-compatibility. Bypasses dynamic matrix and uses static base processing fee.
     */
    public BigDecimal resolveProcessingFee(LoanProduct product, BigDecimal loanAmount) {
        return resolveProcessingFee(product, loanAmount, null);
    }

    /**
     * Resolves the Interest Rate (ROI) for an applicant using the product's ROI matrix.
     * If no matching matrix row is found, it falls back to the base product.getRoi().
     */
    public BigDecimal resolveRoi(LoanProduct product, ApplicantProfile applicant, BigDecimal requestedAmount) {
        if (product.getId() == null) {
            return product.getRoi();
        }

        String activeBundleId = activeBundlePolicyProvider.getActiveBundleId();
        String key1 = product.getId() + ":" + activeBundleId;
        List<ProductRoiMatrix> matrixRows = roiCache.get(key1);
        if (matrixRows == null || matrixRows.isEmpty()) {
            String key2 = product.getId() + ":BASE";
            matrixRows = roiCache.get(key2);
            if (matrixRows == null || matrixRows.isEmpty()) {
                String key3 = product.getId() + ":ALL";
                matrixRows = roiCache.get(key3);
            }
        }

        if (matrixRows == null || matrixRows.isEmpty()) {
            matrixRows = roiMatrixRepository.findByProductIdAndBundleId(product.getId(), activeBundleId);
            if (matrixRows.isEmpty()) {
                matrixRows = roiMatrixRepository.findByProductIdAndBundleId(product.getId(), "BASE");
                if (matrixRows.isEmpty()) {
                    matrixRows = roiMatrixRepository.findByProductId(product.getId()); // absolute fallback
                }
            }
        }

        if (matrixRows == null || matrixRows.isEmpty()) {
            return product.getRoi();
        }

        BigDecimal loanAmount = requestedAmount != null ? requestedAmount : BigDecimal.ZERO;
        String empType = applicant.empType();
        Integer cibil = applicant.cibil();
        boolean isNtc = applicant.isNtc();

        // Find the first matching row in the matrix
        for (ProductRoiMatrix row : matrixRows) {
            // Check NTC flag
            if (row.isNtc() != isNtc) {
                if (row.isNtc() && !isNtc) continue;
                if (!row.isNtc() && isNtc) continue;
            }

            // Check Employment Type
            String rowEmpType = row.getEmploymentType();
            if (rowEmpType != null) {
                if (!centralizedNormalizer.matchRoiEmploymentType(rowEmpType, empType, product.getLenderName())) {
                    continue;
                }
            }

            // Check Loan Amount
            if (row.getMinLoanAmount() != null && loanAmount.compareTo(row.getMinLoanAmount()) < 0) {
                continue;
            }
            if (row.getMaxLoanAmount() != null && loanAmount.compareTo(row.getMaxLoanAmount()) > 0) {
                continue;
            }

            // Check CIBIL (only if not NTC, as NTC ignores min/max cibil)
            if (!isNtc) {
                if (row.getMinCibil() != null && (cibil == null || cibil < row.getMinCibil())) {
                    continue;
                }
                if (row.getMaxCibil() != null && (cibil == null || cibil > row.getMaxCibil())) {
                    continue;
                }
            }

            // If we get here, all constraints matched!
            log.debug("Found matching ROI matrix row for product {} -> ROI: {}", product.getId(), row.getRoi());
            return row.getRoi();
        }

        // Fallback to the product's base ROI if no matrix row matched
        log.warn("No matching ROI matrix row found for product {}, falling back to base ROI: {}", product.getId(), product.getRoi());
        return product.getRoi();
    }

    /**
     * Resolves the absolute login fee (in ₹) for a given product, loan amount, and employment type.
     */
    public BigDecimal resolveLoginFee(LoanProduct product, BigDecimal loanAmount, String employmentType) {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "loanAmount must be a positive value; received: " + loanAmount);
        }

        var active = activeBundlePolicyProvider.getActiveBundle();
        if (active != null && active.loginFeeRules() != null && employmentType != null) {
            for (var row : active.loginFeeRules()) {
                if (product.getProductCode() != null && product.getProductCode().equalsIgnoreCase(row.productName())) {
                    // 1. Check Employment Type
                    String rowEmpType = row.employmentType();
                    if (rowEmpType != null) {
                        if (!centralizedNormalizer.matchRoiEmploymentType(rowEmpType, employmentType, product.getLenderName())) {
                            continue;
                        }
                    }

                    // 2. Check Loan Amount Slabs
                    BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                    if (loanAmount.compareTo(minAmt) < 0 || loanAmount.compareTo(maxAmt) > 0) {
                        continue;
                    }

                    // Found matching slab!
                    BigDecimal fee = row.loginFees();
                    log.debug("Dynamic Login Fee resolved: product={} loanAmount={} → loginFee={}",
                            product.getProductCode(), loanAmount, fee);
                    return fee != null ? fee.setScale(FEE_SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                }
            }
        }

        // Fallback to static login fees in product table
        if (product.getLoginFees() != null) {
            return product.getLoginFees().setScale(FEE_SCALE, RoundingMode.HALF_UP);
        }

        // No fee configured
        log.debug("No static/dynamic login fee for product={}. Returning ZERO.",
                product.getProductCode());
        return BigDecimal.ZERO;
    }

    /**
     * Overloaded method for backward-compatibility. Bypasses dynamic matrix and uses static base login fee.
     */
    public BigDecimal resolveLoginFee(LoanProduct product, BigDecimal loanAmount) {
        return resolveLoginFee(product, loanAmount, null);
    }
}
