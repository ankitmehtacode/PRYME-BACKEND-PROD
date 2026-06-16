package com.pryme.Backend.eligibility.dto;

import java.math.BigDecimal;

/**
 * Projection of a LoanProduct that has been hydrated with applicant-specific
 * dynamic pricing from the {@link com.pryme.Backend.eligibility.service.FinancialComputationEngine}.
 *
 * This DTO is the output of the recommendation pipeline and is intended
 * to be serialized directly to the REST response.
 *
 * @param productId   database surrogate key of the LoanProduct
 * @param productCode stable business identifier (e.g. "LNT-LAP-001")
 * @param bankName    display name of the lending institution
 * @param productName marketing name of the product
 * @param loanType    product category ('HL', 'LAP', 'BL', 'PL', etc.)
 * @param dynamicRoi  applicant-specific ROI (annual %, scale=2) resolved via SpEL
 * @param dynamicPf   applicant-specific processing fee (₹, scale=2) resolved via SpEL
 * @param minLoanAmount minimum sanctionable amount for this product
 * @param maxLoanAmount maximum sanctionable amount for this product
 */
public record RecommendedProductDTO(
        Long productId,
        String productCode,
        String bankName,
        String productName,
        String loanType,
        BigDecimal dynamicRoi,
        BigDecimal dynamicPf,
        BigDecimal dynamicLoginFee,
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        String adminFee,
        String insuranceCharges,
        String legalTechnicalCharges,
        String otherExpense,
        String stampDuty,
        String prepaymentCharges,
        String foreclosureCharges
) {}
