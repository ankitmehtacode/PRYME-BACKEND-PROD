package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.dto.ApplicantProfile;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 FINANCIAL COMPUTATION ENGINE — STATIC FEE RESOLVER
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Resolves processing fees at runtime using the product's static processingFee
 * percentage field. Dynamic SpEL-based computation (ROI, PF, LTV, FOIR) has been
 * removed — real production data will be seeded with the appropriate static values.
 *
 * Fallback chain:
 *   1. processingFee (static %)   → loanAmount × staticRate
 *   2. BigDecimal.ZERO            → no fee
 *
 * @since 2026-04-30
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialComputationEngine {

    private final ProductRoiMatrixRepository roiMatrixRepository;

    /** Scale for all INR fee outputs. */
    private static final int FEE_SCALE = 2;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the absolute processing fee (in ₹) for a given product and loan amount.
     *
     * @param product    the LoanProduct entity (must not be null)
     * @param loanAmount the applicant's requested loan amount (must be > 0)
     * @return           absolute processing fee as BigDecimal, scale=2, never null
     * @throws IllegalArgumentException if loanAmount is null or non-positive
     */
    public BigDecimal resolveProcessingFee(LoanProduct product, BigDecimal loanAmount) {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "loanAmount must be a positive value; received: " + loanAmount);
        }

        // Static percentage: loanAmount × staticRate
        if (product.getProcessingFee() != null) {
            return loanAmount.multiply(product.getProcessingFee())
                    .setScale(FEE_SCALE, RoundingMode.HALF_UP);
        }

        // No fee configured
        log.debug("No static fee for product={}. Returning ZERO.",
                product.getProductCode());
        return BigDecimal.ZERO;
    }

    /**
     * Resolves the Interest Rate (ROI) for an applicant using the product's ROI matrix.
     * If no matching matrix row is found, it falls back to the base product.getRoi().
     *
     * @param product   the loan product
     * @param applicant the applicant profile
     * @return the resolved ROI (e.g., 0.0825 for 8.25%)
     */
    public BigDecimal resolveRoi(LoanProduct product, ApplicantProfile applicant, BigDecimal requestedAmount) {
        if (product.getId() == null) {
            return product.getRoi();
        }

        List<ProductRoiMatrix> matrixRows = roiMatrixRepository.findByProductId(product.getId());
        
        if (matrixRows.isEmpty()) {
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
                // If the row is specifically for NTC, but applicant isn't NTC (or vice versa), skip.
                // However, if the row has isNtc=false, it means it's for regular applicants.
                if (row.isNtc() && !isNtc) continue;
                if (!row.isNtc() && isNtc) continue;
            }

            // Check Employment Type
            String rowEmpType = row.getEmploymentType();
            if (rowEmpType != null) {
                boolean match = false;
                if (rowEmpType.equalsIgnoreCase("SALARIED_SEP")) {
                    match = empType.equalsIgnoreCase("Salaried") || empType.equalsIgnoreCase("SEP/SENP");
                } else if (rowEmpType.equalsIgnoreCase("SEP_SENP") 
                        || rowEmpType.equalsIgnoreCase("SENP") 
                        || rowEmpType.equalsIgnoreCase("SENP (Industry Margin)")) {
                    match = empType.equalsIgnoreCase("SEP/SENP");
                } else {
                    match = rowEmpType.equalsIgnoreCase(empType);
                }
                if (!match) {
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
}
