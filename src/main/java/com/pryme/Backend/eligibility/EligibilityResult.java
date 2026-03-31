package com.pryme.Backend.eligibility.dto;

import com.pryme.Backend.eligibility.LapProgram;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 🧠 CORE DOMAIN CONTRACT: The Unified Eligibility Result
 * Combines strict modular routing with the rich financial analytics
 * (FOIR, EMI, Max Loan) required by the Admin Dashboard.
 */
public record EligibilityResult
        (
        // ── Routing & State (The New Engine Standard) ──
        boolean isEligible,
        String productCode,
        String productName,
        List<String> violations,
        String reason,

        // ── Financial Analytics (Legacy Features Preserved for Admin Dashboard) ──
        LapProgram program,
        BigDecimal eligibleIncome,
        BigDecimal maxEmiAllowed,
        BigDecimal netEligibleEmi,
        BigDecimal foirPercent,
        BigDecimal deviationPercent,
        String formula,
        BigDecimal minimumLoanAmount,
        BigDecimal maximumLoanAmount,
        BigDecimal estimatedEligibleLoanAmount,
        Integer emiNotObligatedMonths,
        String conditions,
        String specialNotes,
        LocalDate lastUpdatedOn
) {
    // 1. FACTORY: Preflight Rejection (Fast Fail - Saves CPU cycles)
    public static EligibilityResult rejected(List<String> violations, String reason) {
        return new EligibilityResult(
                false, null, null, violations, reason,
                null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, "N/A",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                0, null, null, LocalDate.now()
        );
    }

    // 2. FACTORY: Product-Level Ineligibility
    public static EligibilityResult ineligible(String code, String name, List<String> violations, String reason) {
        return new EligibilityResult(
                false, code, name, violations, reason,
                null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, "N/A",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                0, null, null, LocalDate.now()
        );
    }

    // 3. FACTORY: Standard Eligibility
    public static EligibilityResult eligible(String code, String name, BigDecimal amount) {
        return new EligibilityResult(
                true, code, name, List.of(), "Passed",
                null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, "Standard",
                BigDecimal.ZERO, BigDecimal.ZERO, amount,
                0, null, null, LocalDate.now()
        );
    }

    // ── LEGACY ALIASES: Prevents frontend/controller crashes ──
    public boolean eligible() { return isEligible; }
    public List<String> reasons() { return violations; }
}