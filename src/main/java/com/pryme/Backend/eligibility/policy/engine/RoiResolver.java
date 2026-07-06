package com.pryme.Backend.eligibility.policy.engine;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.model.ProductRoiMatrixRule;
import java.math.BigDecimal;

public class RoiResolver {
    public BigDecimal resolve(PolicyBundle bundle, Long productId, String employmentType, BigDecimal loanAmount, int cibil, boolean isNtc, BigDecimal defaultRoi) {
        if (bundle.roiRules() == null || bundle.roiRules().isEmpty()) {
            return defaultRoi;
        }

        for (ProductRoiMatrixRule row : bundle.roiRules()) {
            if (row.productId() != null && !row.productId().equals(productId)) {
                continue;
            }
            if (row.ntc() != isNtc) {
                continue;
            }

            String rowEmpType = row.employmentType();
            if (rowEmpType != null) {
                boolean match = false;
                if (rowEmpType.equalsIgnoreCase("SALARIED_SEP")) {
                    match = employmentType.equalsIgnoreCase("Salaried") || employmentType.equalsIgnoreCase("SEP/SENP");
                } else if (rowEmpType.equalsIgnoreCase("SEP_SENP") 
                        || rowEmpType.equalsIgnoreCase("SENP") 
                        || rowEmpType.equalsIgnoreCase("SEP")) {
                    match = employmentType.equalsIgnoreCase("Self Employed Professional") 
                            || employmentType.equalsIgnoreCase("Self Employed Non Professional")
                            || employmentType.equalsIgnoreCase("SEP/SENP");
                } else {
                    match = rowEmpType.equalsIgnoreCase(employmentType);
                }
                if (!match) continue;
            }

            BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
            BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
            if (loanAmount.compareTo(minAmt) < 0 || loanAmount.compareTo(maxAmt) > 0) {
                continue;
            }

            Integer minCibil = row.minCibil();
            Integer maxCibil = row.maxCibil();
            if (minCibil != null && cibil < minCibil) continue;
            if (maxCibil != null && cibil > maxCibil) continue;

            return row.roi();
        }
        return defaultRoi;
    }
}
