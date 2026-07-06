package com.pryme.Backend.eligibility.policy.engine;

import com.pryme.Backend.eligibility.policy.model.ProcessingFeeRule;
import com.pryme.Backend.eligibility.policy.model.LoginFeeRule;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import java.math.BigDecimal;

public class FeeResolver {
    public BigDecimal resolveProcessingFee(PolicyBundle bundle, String lenderName, String loanType, String employmentType, BigDecimal loanAmount) {
        if (bundle.pfRules() == null) return BigDecimal.ZERO;
        
        String normLender = normalize(lenderName);
        String normEmp = normalize(employmentType);
        String normLoan = normalize(loanType);
        
        for (var row : bundle.pfRules()) {
            if (normalize(row.lenderName()).equalsIgnoreCase(normLender)
                && normalize(row.loanType()).equalsIgnoreCase(normLoan)
                && normalize(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
                BigDecimal min = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                BigDecimal max = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                
                if (loanAmount.compareTo(min) >= 0 && loanAmount.compareTo(max) <= 0) {
                    return row.pf();
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal resolveLoginFee(PolicyBundle bundle, String lenderName, String loanType, String employmentType, BigDecimal loanAmount) {
        if (bundle.loginFeeRules() == null) return BigDecimal.ZERO;
        
        String normLender = normalize(lenderName);
        String normEmp = normalize(employmentType);
        String normLoan = normalize(loanType);
        
        for (var row : bundle.loginFeeRules()) {
            if (normalize(row.lenderName()).equalsIgnoreCase(normLender)
                && normalize(row.loanType()).equalsIgnoreCase(normLoan)
                && normalize(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
                BigDecimal min = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                BigDecimal max = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                
                if (loanAmount.compareTo(min) >= 0 && loanAmount.compareTo(max) <= 0) {
                    return row.loginFees();
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private String normalize(String val) {
        if (val == null) return "";
        return val.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
