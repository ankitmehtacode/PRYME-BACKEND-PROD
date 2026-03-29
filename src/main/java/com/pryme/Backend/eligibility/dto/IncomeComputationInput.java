package com.pryme.Backend.eligibility.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 🧠 CORE DOMAIN OBJECT: Feeds the SurrogateIncomeResolver.
 * Encapsulates all raw financial inputs required to calculate surrogate eligibility
 * (e.g., Banking Surrogate, GST Surrogate, ITR Multiplier).
 */
public record IncomeComputationInput(
        
        @DecimalMin("0.00") 
        BigDecimal averageMonthlyBankBalance,
        
        @DecimalMin("0.00") 
        BigDecimal latestYearGrossReceipts,
        
        @DecimalMin("0.00") 
        BigDecimal latestYearNetProfit,
        
        @DecimalMin("0.00") 
        BigDecimal depreciationAddedBack,
        
        @DecimalMin("0.00") 
        BigDecimal partnerRemunerationAddedBack,
        
        boolean isAudited
) {
    // Compact constructor for defensive null-handling
    public IncomeComputationInput {
        averageMonthlyBankBalance = safe(averageMonthlyBankBalance);
        latestYearGrossReceipts = safe(latestYearGrossReceipts);
        latestYearNetProfit = safe(latestYearNetProfit);
        depreciationAddedBack = safe(depreciationAddedBack);
        partnerRemunerationAddedBack = safe(partnerRemunerationAddedBack);
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
