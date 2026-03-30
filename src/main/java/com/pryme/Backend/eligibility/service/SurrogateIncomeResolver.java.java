// File: src/main/java/com/pryme/Backend/eligibility/service/SurrogateIncomeResolver.java

package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.exception.SurrogatePolicyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Resolves surrogate income for each L&T Finance LAP program.
 *
 * Formulas sourced from Banking_Policies_Template_D1.xlsx:
 *   NIP      : (PAT + Depreciation + Interest) / 12          → monthly
 *   Banking  : Average Bank Balance (ABB of 5/10/20/25 dates) → already monthly
 *   GST      : (12M GSTR-3B Turnover × margin) / 12          → monthly
 *   CashFlow : same as Banking ABB                            → monthly
 *   SENP     : (Gross Receipts × multiplier) / 12             → monthly
 *
 * Returns: monthly income as BigDecimal, DECIMAL128 precision.
 * Caller (EligibilityEngineService) applies FOIR against this monthly figure.
 */
@Service
@Slf4j
public class SurrogateIncomeResolver {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal TWELVE = new BigDecimal("12");

    // GST profit margins per business type — sourced from Excel
    private static final Map<String, BigDecimal> GST_MARGINS = Map.of(
            "Service",       new BigDecimal("0.10"),
            "Retail",        new BigDecimal("0.12"),
            "Wholesale",     new BigDecimal("0.08"),
            "Manufacturing", new BigDecimal("0.04")
    );

    /**
     * Resolves computed monthly income for the given program.
     *
     * @param input  income inputs for the applicant
     * @return       computed monthly income (BigDecimal, never null)
     * @throws SurrogatePolicyNotFoundException if the program name is unrecognised
     */
    public BigDecimal resolve(IncomeComputationInput input) {
        if (input == null || input.programName() == null) {
            throw new SurrogatePolicyNotFoundException(
                    "IncomeComputationInput or programName must not be null");
        }

        BigDecimal monthly = switch (input.programName().toUpperCase()) {
            case "NIP"      -> resolveNip(input);
            case "BANKING"  -> resolveBanking(input);
            case "GST"      -> resolveGst(input);
            case "CASHFLOW" -> resolveCashFlow(input);
            case "SENP"     -> resolveSenp(input);
            default -> throw new SurrogatePolicyNotFoundException(
                    "No surrogate policy found for program: " + input.programName());
        };

        log.debug("SurrogateIncomeResolver: program={} monthlyIncome={}",
                input.programName(), monthly.setScale(2, RoundingMode.HALF_UP));
        return monthly;
    }

    // ─── NIP: (PAT + Depreciation + Interest) ÷ 12 ────────────────────────
    private BigDecimal resolveNip(IncomeComputationInput input) {
        BigDecimal annual = safe(input.pat())
                .add(safe(input.depreciation()), MC)
                .add(safe(input.interestExpense()), MC);
        return annual.divide(TWELVE, MC);
    }

    // ─── Banking: Average Bank Balance (5/10/20/25 dates), up to 4 accounts ─
    private BigDecimal resolveBanking(IncomeComputationInput input) {
        List<BigDecimal> samples = input.bankBalanceSamples();
        if (samples != null && !samples.isEmpty()) {
            BigDecimal sum = samples.stream()
                    .map(this::safe)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b, MC));
            return sum.divide(BigDecimal.valueOf(samples.size()), MC);
        }
        // Fallback: caller provided a pre-computed ABB
        return safe(input.averageBankBalance());
    }

    // ─── GST: (Last 12M GSTR-3B Turnover × profit margin) ÷ 12 ───────────
    private BigDecimal resolveGst(IncomeComputationInput input) {
        String businessType = input.businessType() != null ? input.businessType() : "";
        BigDecimal margin = GST_MARGINS.getOrDefault(businessType, BigDecimal.ZERO);
        return safe(input.gstrTurnover12Months())
                .multiply(margin, MC)
                .divide(TWELVE, MC);
    }

    // ─── CashFlow: same ABB formula as Banking, LTV deviation handled by engine
    private BigDecimal resolveCashFlow(IncomeComputationInput input) {
        return resolveBanking(input);
    }

    // ─── SENP: (Gross Receipts × multiplier) ÷ 12
    //     Multiplier: CS = 1.5, all others = 2.5  (from Excel)
    private BigDecimal resolveSenp(IncomeComputationInput input) {
        BigDecimal multiplier = "CS".equalsIgnoreCase(input.profession())
                ? new BigDecimal("1.5")
                : new BigDecimal("2.5");
        return safe(input.grossReceipts())
                .multiply(multiplier, MC)
                .divide(TWELVE, MC);
    }

    private BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
package com.pryme.Backend.eligibility.service;
import com.pryme.Backend.eligibility.dto.PreflightRequest;
import com.pryme.Backend.eligibility.dto.PreflightResult;

public interface GeneralPolicyPreflightService {
    PreflightResult evaluate(PreflightRequest request);
}
