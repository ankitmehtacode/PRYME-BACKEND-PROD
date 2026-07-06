package com.pryme.Backend.eligibility.policy.engine;

import com.pryme.Backend.eligibility.policy.model.LowLtvRule;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import java.math.BigDecimal;

public class LowLtvResolver {
    public BigDecimal resolve(PolicyBundle bundle, String loanType, String lenderName, String propertyType, BigDecimal loanAmount) {
        if (bundle.lowLtvRules() == null) return null;

        String normLoan = loanType != null ? loanType.toUpperCase() : "HL";
        if ("LAP".equals(normLoan)) {
            for (var row : bundle.lowLtvRules()) {
                if (!"LAP".equalsIgnoreCase(row.loanType())) continue;
                if (lenderName != null && isLenderMatch(lenderName, row.lenderName())) {
                    if (propertyType != null && propertyType.equalsIgnoreCase(row.propertyType())) {
                        String ltvStr = row.ltvValue();
                        if (ltvStr == null || ltvStr.equalsIgnoreCase("Negative") || ltvStr.equalsIgnoreCase("N/A")) {
                            return BigDecimal.ZERO;
                        }
                        try {
                            return new BigDecimal(ltvStr);
                        } catch (Exception e) {
                            return BigDecimal.ZERO;
                        }
                    }
                }
            }
        } else {
            for (var row : bundle.lowLtvRules()) {
                if (!"HL".equalsIgnoreCase(row.loanType())) continue;
                if (isHlPropertyMatch(propertyType, row.propertyType())) {
                    BigDecimal amount = loanAmount != null ? loanAmount : BigDecimal.ZERO;
                    BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                    if (amount.compareTo(minAmt) >= 0 && amount.compareTo(maxAmt) <= 0) {
                        String ltvStr = row.ltvValue();
                        if (ltvStr == null) return BigDecimal.ZERO;
                        try {
                            return new BigDecimal(ltvStr);
                        } catch (Exception e) {
                            return BigDecimal.ZERO;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isLenderMatch(String inputName, String targetName) {
        if (inputName == null || targetName == null) return false;
        String lowerInput = inputName.toLowerCase().replaceAll("[^a-z0-9]", "");
        String lowerTarget = targetName.toLowerCase().replaceAll("[^a-z0-9]", "");
        return lowerInput.contains(lowerTarget) || lowerTarget.contains(lowerInput);
    }

    private boolean isHlPropertyMatch(String inputType, String targetType) {
        if (inputType == null || targetType == null) return false;
        String lowerInput = inputType.toLowerCase();
        String lowerTarget = targetType.toLowerCase();
        if (lowerTarget.contains("plot") || lowerTarget.contains("land")) {
            return lowerInput.contains("plot") || lowerInput.contains("land");
        }
        return !lowerInput.contains("plot") && !lowerInput.contains("land");
    }
}
