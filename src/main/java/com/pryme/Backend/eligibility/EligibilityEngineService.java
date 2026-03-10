package com.pryme.Backend.eligibility;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class EligibilityEngineService {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final Map<LapProgram, ProgramPolicy> POLICIES = Map.of(
            LapProgram.NIP, new ProgramPolicy(
                    "PAT + Depreciation + Interest",
                    new BigDecimal("95"),
                    BigDecimal.ZERO,
                    3,
                    2,
                    0,
                    new BigDecimal("100000"),
                    new BigDecimal("20000000"),
                    "Upto 2CR - CA Certified, Above 2CR - Audited",
                    "Normal Income Program"
            ),
            LapProgram.BANKING, new ProgramPolicy(
                    "ABB-based banking surrogate",
                    new BigDecimal("55"),
                    new BigDecimal("10"),
                    3,
                    2,
                    6,
                    new BigDecimal("100000"),
                    new BigDecimal("15000000"),
                    "Upto 4 account statements of applicant & co-applicant (SBA & CA)",
                    "10% deviation against LTV"
            ),
            LapProgram.GST, new ProgramPolicy(
                    "Last 12M GSTR-3B Turnover * Profit Margin",
                    new BigDecimal("65"),
                    BigDecimal.ZERO,
                    3,
                    2,
                    0,
                    new BigDecimal("100000"),
                    new BigDecimal("18000000"),
                    "Profit Margin: Service 10%, Retailer 12%, Wholesaler 8%, Manufacturer 4%",
                    "Program specific margin-based underwriting"
            ),
            LapProgram.CASH_FLOW, new ProgramPolicy(
                    "ABB cash-flow surrogate",
                    new BigDecimal("182"),
                    BigDecimal.ZERO,
                    5,
                    1,
                    0,
                    new BigDecimal("100000"),
                    new BigDecimal("12000000"),
                    "Residential cases capped at 55% LTV",
                    "LTV reduced to 55% on residential"
            ),
            LapProgram.SENP, new ProgramPolicy(
                    "Gross Receipts * 2.5",
                    new BigDecimal("75"),
                    BigDecimal.ZERO,
                    2,
                    2,
                    0,
                    new BigDecimal("100000"),
                    new BigDecimal("25000000"),
                    "SENP (CA/CS/Doctor)",
                    "Multiplier up to 1.5 for CS profile"
            )
    );

    public BestMatchResponse evaluate(EligibilityRequest request) {
        List<EligibilityResult> evaluated = List.of(
                evaluateNip(request),
                evaluateBanking(request),
                evaluateGst(request),
                evaluateCashFlow(request),
                evaluateSenp(request)
        );

        EligibilityResult best = evaluated.stream()
                .filter(EligibilityResult::eligible)
                .max(Comparator.comparing(EligibilityResult::estimatedEligibleLoanAmount))
                .orElseGet(() -> evaluated.stream().max(Comparator.comparing(EligibilityResult::netEligibleEmi)).orElseThrow());

        return new BestMatchResponse(best, evaluated);
    }

    EligibilityResult evaluateNip(EligibilityRequest req) {
        BigDecimal income = req.pat().add(req.depreciation(), MC).add(req.interest(), MC);
        return finalizeResult(req, LapProgram.NIP, income, Map.of());
    }

    EligibilityResult evaluateBanking(EligibilityRequest req) {
        BigDecimal effectiveEmis = req.emiObligationAgeMonths() < 6 ? BigDecimal.ZERO : req.existingEmis();
        return finalizeResult(req, LapProgram.BANKING, req.avgBankCredits(), Map.of("effectiveEmis", effectiveEmis));
    }

    EligibilityResult evaluateGst(EligibilityRequest req) {
        BigDecimal margin = switch (req.businessType()) {
            case SERVICE -> new BigDecimal("0.10");
            case RETAILER -> new BigDecimal("0.12");
            case WHOLESALER -> new BigDecimal("0.08");
            case MANUFACTURER -> new BigDecimal("0.04");
            case OTHER -> BigDecimal.ZERO;
        };
        return finalizeResult(req, LapProgram.GST, req.turnover12M().multiply(margin, MC), Map.of());
    }

    EligibilityResult evaluateCashFlow(EligibilityRequest req) {
        return finalizeResult(req, LapProgram.CASH_FLOW, req.avgBankCredits(), Map.of("ltvCapResidential", new BigDecimal("55")));
    }

    EligibilityResult evaluateSenp(EligibilityRequest req) {
        return finalizeResult(req, LapProgram.SENP, req.grossReceipts().multiply(new BigDecimal("2.5"), MC), Map.of());
    }

    private EligibilityResult finalizeResult(
            EligibilityRequest req,
            LapProgram program,
            BigDecimal income,
            Map<String, BigDecimal> opts
    ) {
        ProgramPolicy policy = POLICIES.get(program);

        List<String> reasons = new ArrayList<>();
        boolean eligible = true;

        if (req.businessVintageYears() < policy.minVintageYears) {
            eligible = false;
            reasons.add("Business vintage below required " + policy.minVintageYears + " years");
        }
        if (req.itrYears() < policy.minItrYears) {
            eligible = false;
            reasons.add("ITR years below required " + policy.minItrYears + " years");
        }

        if (program == LapProgram.CASH_FLOW && req.residentialProperty()) {
            BigDecimal cap = opts.getOrDefault("ltvCapResidential", new BigDecimal("55"));
            if (req.requestedLtvPercent().compareTo(cap) > 0) {
                eligible = false;
                reasons.add("Residential LTV above " + cap + "% for Cash Flow program");
            }
        }

        BigDecimal foirFactor = policy.foirPercent.divide(HUNDRED, MC);
        BigDecimal maxEmi = income.multiply(foirFactor, MC);
        BigDecimal effectiveEmis = opts.getOrDefault("effectiveEmis", req.existingEmis());
        BigDecimal netEligible = maxEmi.subtract(effectiveEmis, MC);

        if (netEligible.compareTo(BigDecimal.ZERO) < 0) {
            eligible = false;
            reasons.add("Existing EMI obligations exceed allowed FOIR capacity");
        }

        BigDecimal estimatedLoanAmount = presentValueFromEmi(netEligible.max(BigDecimal.ZERO), req.annualInterestRate(), req.tenureMonths());

        if (estimatedLoanAmount.compareTo(policy.minimumLoanAmount) < 0) {
            eligible = false;
            reasons.add("Below minimum loan amount threshold");
        }
        if (estimatedLoanAmount.compareTo(policy.maximumLoanAmount) > 0) {
            reasons.add("Capped by maximum loan amount policy");
        }

        BigDecimal policyCapped = estimatedLoanAmount.min(policy.maximumLoanAmount);
        BigDecimal finalLoan = policyCapped.min(req.requestedLoanAmount());

        return new EligibilityResult(
                program,
                scale(income),
                scale(maxEmi),
                scale(netEligible.max(BigDecimal.ZERO)),
                scale(policy.foirPercent),
                scale(policy.deviationPercent),
                policy.formula,
                eligible,
                reasons,
                scale(policy.minimumLoanAmount),
                scale(policy.maximumLoanAmount),
                scale(finalLoan.max(BigDecimal.ZERO)),
                policy.emiNotObligatedMonths,
                policy.conditions,
                policy.specialNotes,
                LocalDate.now()
        );
    }

    private BigDecimal presentValueFromEmi(BigDecimal emi, BigDecimal annualRatePercent, int tenureMonths) {
        if (emi.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = annualRatePercent.divide(new BigDecimal("1200"), MC);
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return emi.multiply(BigDecimal.valueOf(tenureMonths), MC);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal discount = BigDecimal.ONE;
        BigDecimal sumFactor = BigDecimal.ZERO;
        for (int k = 1; k <= tenureMonths; k++) {
            discount = discount.multiply(onePlusR, MC);
            sumFactor = sumFactor.add(BigDecimal.ONE.divide(discount, MC), MC);
        }
        return emi.multiply(sumFactor, MC);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    private record ProgramPolicy(
            String formula,
            BigDecimal foirPercent,
            BigDecimal deviationPercent,
            int minVintageYears,
            int minItrYears,
            int emiNotObligatedMonths,
            BigDecimal minimumLoanAmount,
            BigDecimal maximumLoanAmount,
            String conditions,
            String specialNotes
    ) {
    }
}
