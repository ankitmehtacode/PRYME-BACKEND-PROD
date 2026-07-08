package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.dto.ApplicantProfile;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.entity.ProductLoginFeeMatrix;
import com.pryme.Backend.loanproduct.entity.ProductPfMatrix;
import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import com.pryme.Backend.loanproduct.repository.ProductLoginFeeMatrixRepository;
import com.pryme.Backend.loanproduct.repository.ProductPfMatrixRepository;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import com.pryme.Backend.loanproduct.entity.ProductFoirMatrix;
import com.pryme.Backend.loanproduct.repository.ProductFoirMatrixRepository;
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
 * Resolves processing fees and interest rates (ROI) at runtime using product-specific matrices.
 * Fallback to base product parameters.
 *
 * @since 2026-06-12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialComputationEngine {

    private final ProductRoiMatrixRepository roiMatrixRepository;
    private final ProductPfMatrixRepository pfMatrixRepository;
    private final ProductLoginFeeMatrixRepository loginFeeMatrixRepository;
    private final ProductFoirMatrixRepository foirMatrixRepository;

    /** Scale for all INR fee outputs. */
    private static final int FEE_SCALE = 2;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the absolute processing fee (in ₹) for a given product, loan amount, and employment type.
     *
     * @param product        the LoanProduct entity (must not be null)
     * @param loanAmount     the applicant's requested loan amount (must be > 0)
     * @param employmentType the applicant's employment type (e.g. 'Salaried', 'SEP/SENP')
     * @return               absolute processing fee as BigDecimal, scale=2, never null
     * @throws IllegalArgumentException if loanAmount is null or non-positive
     */
    public BigDecimal resolveProcessingFee(LoanProduct product, BigDecimal loanAmount, String employmentType) {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "loanAmount must be a positive value; received: " + loanAmount);
        }

        if (product.getId() != null && employmentType != null) {
            List<ProductPfMatrix> matrixRows = pfMatrixRepository.findByProductId(product.getId());
            if (matrixRows != null && !matrixRows.isEmpty()) {
                for (ProductPfMatrix row : matrixRows) {
                    // 1. Check Employment Type
                    String rowEmpType = row.getEmploymentType();
                        boolean match = EligibilityEngineService.matchEmploymentType(rowEmpType, employmentType);
                        
                        if (!match) {
                            continue;
                        }

                    // 2. Check Loan Amount Slabs
                    if (row.getMinLoanAmount() != null && loanAmount.compareTo(row.getMinLoanAmount()) < 0) {
                        continue;
                    }
                    if (row.getMaxLoanAmount() != null && loanAmount.compareTo(row.getMaxLoanAmount()) > 0) {
                        continue;
                    }

                    // Found matching slab!
                    BigDecimal baseFee;
                    if (row.isFlat()) {
                        baseFee = row.getFeeValue();
                    } else {
                        baseFee = loanAmount.multiply(row.getFeeValue());
                    }

                    // Apply Min/Max Fee limits
                    if (row.getMinFee() != null && baseFee.compareTo(row.getMinFee()) < 0) {
                        baseFee = row.getMinFee();
                    }
                    if (row.getMaxFee() != null && baseFee.compareTo(row.getMaxFee()) > 0) {
                        baseFee = row.getMaxFee();
                    }

                    // Apply Tax Rate (total = baseFee * (1 + taxRate))
                    BigDecimal taxRate = row.getTaxRate() != null ? row.getTaxRate() : new BigDecimal("0.1800");
                    BigDecimal multiplier = BigDecimal.ONE.add(taxRate);
                    BigDecimal totalFee = baseFee.multiply(multiplier);

                    log.debug("Dynamic PF resolved: product={} loanAmount={} baseFee={} taxRate={} → totalFee={}",
                            product.getProductCode(), loanAmount, baseFee, taxRate, totalFee);

                    return totalFee.setScale(FEE_SCALE, RoundingMode.HALF_UP);
                }
            }
        }

        // No dynamic fee configured in the matrix
        log.debug("No dynamic processing fee matrix entry found for product={} and employment={}. Returning ZERO.",
                product.getProductCode(), employmentType);
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
                boolean match = EligibilityEngineService.matchEmploymentType(rowEmpType, empType);
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

    /**
     * Resolves the absolute login fee (in ₹) for a given product, loan amount, and employment type.
     *
     * @param product        the LoanProduct entity (must not be null)
     * @param loanAmount     the applicant's requested loan amount (must be > 0)
     * @param employmentType the applicant's employment type (e.g. 'Salaried', 'SEP/SENP')
     * @return               absolute login fee as BigDecimal, scale=2, never null
     * @throws IllegalArgumentException if loanAmount is null or non-positive
     */
    public BigDecimal resolveLoginFee(LoanProduct product, BigDecimal loanAmount, String employmentType) {
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "loanAmount must be a positive value; received: " + loanAmount);
        }

        if (product.getId() != null && employmentType != null) {
            List<ProductLoginFeeMatrix> matrixRows = loginFeeMatrixRepository.findByProductId(product.getId());
            if (matrixRows != null && !matrixRows.isEmpty()) {
                for (ProductLoginFeeMatrix row : matrixRows) {
                    // 1. Check Employment Type
                    String rowEmpType = row.getEmploymentType();
                    if (rowEmpType != null) {
                        boolean match = EligibilityEngineService.matchEmploymentType(rowEmpType, employmentType);
                        if (!match) {
                            continue;
                        }
                    }

                    // 2. Check Loan Amount Slabs
                    if (row.getMinLoanAmount() != null && loanAmount.compareTo(row.getMinLoanAmount()) < 0) {
                        continue;
                    }
                    if (row.getMaxLoanAmount() != null && loanAmount.compareTo(row.getMaxLoanAmount()) > 0) {
                        continue;
                    }

                    // Found matching slab!
                    BigDecimal fee = row.getLoginFee();
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
    
    /**
     * Resolves the maximum allowed FOIR for an applicant using the product's FOIR matrix.
     * Incorporates deviation limits if configured.
     * Fallback to product.getMaxEmiNmiRatio() or default 0.65.
     */
    public BigDecimal resolveFoir(LoanProduct product, ApplicantProfile applicant, BigDecimal effectiveIncome, String surrogate) {
        if (product.getId() == null) {
            return null;
        }

        List<ProductFoirMatrix> matrixRows = foirMatrixRepository.findByProductId(product.getId());
        
        if (matrixRows == null || matrixRows.isEmpty()) {
            BigDecimal maxFoir = product.getMaxEmiNmiRatio();
            if (maxFoir != null && maxFoir.compareTo(BigDecimal.ZERO) > 0) {
                return maxFoir.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
            }
            return null;
        }

        String empType = applicant.empType();

        System.out.println("====== [DEBUG] FOIR MATCHING FOR PRODUCT: " + product.getProductCode() + " ======");
        System.out.println("  Required EmpType: " + empType);
        System.out.println("  Required Surrogate: " + surrogate);
        System.out.println("  Effective Income: " + effectiveIncome);
        for (ProductFoirMatrix row : matrixRows) {
            System.out.println("  --> MATRIX ROW: " + row.getId() + " | Emp: " + row.getEmploymentType() + " | Surr: " + row.getSurrogate() + " | Min: " + row.getMinSalary() + " | Max: " + row.getMaxSalary() + " | Foir: " + row.getFoir());
        }

        for (ProductFoirMatrix row : matrixRows) {
            // Check Employment Type
            String rowEmpType = row.getEmploymentType();
            if (rowEmpType != null && !rowEmpType.isEmpty()) {
                boolean match = EligibilityEngineService.matchEmploymentType(rowEmpType, empType);
                if (!match) continue;
            }

            // Check Surrogate
            String rowSurrogate = row.getSurrogate();
            if (rowSurrogate != null && !rowSurrogate.isEmpty()) {
                if (surrogate == null || !rowSurrogate.equalsIgnoreCase(surrogate)) {
                    continue;
                }
            }

            // Check Income Slabs
            if (row.getMinSalary() != null && effectiveIncome.compareTo(row.getMinSalary()) < 0) {
                continue;
            }
            if (row.getMaxSalary() != null && effectiveIncome.compareTo(row.getMaxSalary()) > 0) {
                continue;
            }

            // Match found!
            BigDecimal finalFoir = row.getFoir();
            if (row.getDeviation() != null) {
                finalFoir = finalFoir.add(row.getDeviation());
            }
            
            log.debug("Found matching FOIR matrix row for product {} -> baseFOIR: {}, dev: {}, final: {}", 
                product.getProductCode(), row.getFoir(), row.getDeviation(), finalFoir);
            return finalFoir;
        }

        // Fallback
        return null;
    }

    public BigDecimal resolveLoginFee(LoanProduct product, BigDecimal loanAmount) {
        return resolveLoginFee(product, loanAmount, null);
    }
}
