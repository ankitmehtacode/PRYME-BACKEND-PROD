package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.PreflightRequest;
import com.pryme.Backend.eligibility.dto.PreflightResult;
import com.pryme.Backend.eligibility.exception.SurrogatePolicyNotFoundException;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.eligibility.service.GeneralPolicyPreflightService;
import com.pryme.Backend.eligibility.resolver.SurrogateIncomeResolver;
import com.pryme.Backend.loanproduct.LoanProduct;
import com.pryme.Backend.loanproduct.LoanProductRepository; // Correct Modular Boundary

@Service
public class EligibilityEngineService {

    // 🧠 High-precision matrix for financial operations to prevent floating-point drift
    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");

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
                    new BigDecimal("182"), // Multiplier logic
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

        // 🧠 Safe resolution fallback: Defends against total engine failure
        EligibilityResult best = evaluated.stream()
                .filter(EligibilityResult::eligible)
                .max(Comparator.comparing(EligibilityResult::estimatedEligibleLoanAmount))
                .orElseGet(() -> evaluated.stream()
                        .max(Comparator.comparing(EligibilityResult::netEligibleEmi))
                        .orElseThrow(() -> new IllegalStateException("Engine failed to compute baseline matrix.")));

        return new BestMatchResponse(best, evaluated);
    }

    // ==========================================
    // 🧠 SURROGATE EVALUATORS (Notice the isAnnualIncome flag)
    // ==========================================

    EligibilityResult evaluateNip(EligibilityRequest req) {
        BigDecimal income = safe(req.pat()).add(safe(req.depreciation()), MC).add(safe(req.interest()), MC);
        return finalizeResult(req, LapProgram.NIP, income, true, Map.of()); // TRUE = Annual
    }

    EligibilityResult evaluateBanking(EligibilityRequest req) {
        BigDecimal effectiveEmis = (req.emiObligationAgeMonths() != null && req.emiObligationAgeMonths() < 6)
                ? BigDecimal.ZERO : safe(req.existingEmis());
        return finalizeResult(req, LapProgram.BANKING, safe(req.avgBankCredits()), false, Map.of("effectiveEmis", effectiveEmis)); // FALSE = Monthly
    }

    EligibilityResult evaluateGst(EligibilityRequest req) {
        BigDecimal margin = req.businessType() != null ? switch (req.businessType()) {
            case SERVICE -> new BigDecimal("0.10");
            case RETAILER -> new BigDecimal("0.12");
            case WHOLESALER -> new BigDecimal("0.08");
            case MANUFACTURER -> new BigDecimal("0.04");
            case OTHER -> BigDecimal.ZERO;
        } : BigDecimal.ZERO;
        return finalizeResult(req, LapProgram.GST, safe(req.turnover12M()).multiply(margin, MC), true, Map.of()); // TRUE = Annual
    }

    EligibilityResult evaluateCashFlow(EligibilityRequest req) {
        return finalizeResult(req, LapProgram.CASH_FLOW, safe(req.avgBankCredits()), false, Map.of("ltvCapResidential", new BigDecimal("55"))); // FALSE = Monthly
    }

    EligibilityResult evaluateSenp(EligibilityRequest req) {
        return finalizeResult(req, LapProgram.SENP, safe(req.grossReceipts()).multiply(new BigDecimal("2.5"), MC), true, Map.of()); // TRUE = Annual
    }

    // ==========================================
    // 🧠 THE CORE UNDERWRITING ENGINE
    // ==========================================

    private EligibilityResult finalizeResult(
            EligibilityRequest req,
            LapProgram program,
            BigDecimal rawIncome,
            boolean isAnnualIncome,
            Map<String, BigDecimal> opts
    ) {
        ProgramPolicy policy = POLICIES.get(program);
        List<String> reasons = new ArrayList<>();
        boolean eligible = true;

        // 🧠 Null-Safe Checks
        if (req.businessVintageYears() != null && req.businessVintageYears() < policy.minVintageYears) {
            eligible = false;
            reasons.add("Business vintage below required " + policy.minVintageYears + " years");
        }
        if (req.itrYears() != null && req.itrYears() < policy.minItrYears) {
            eligible = false;
            reasons.add("ITR years below required " + policy.minItrYears + " years");
        }

        // 🧠 Avoid NPE by using Boolean.TRUE.equals()
        if (program == LapProgram.CASH_FLOW && Boolean.TRUE.equals(req.residentialProperty())) {
            BigDecimal cap = opts.getOrDefault("ltvCapResidential", new BigDecimal("55"));
            if (req.requestedLtvPercent() != null && req.requestedLtvPercent().compareTo(cap) > 0) {
                eligible = false;
                reasons.add("Residential LTV above " + cap + "% for Cash Flow program");
            }
        }

        // 🧠 CRITICAL FIX: The "12x Bankruptcy" Resolution
        // Normalize everything to a Monthly Income before applying the FOIR percentage
        BigDecimal monthlyIncome = isAnnualIncome ? rawIncome.divide(TWELVE, MC) : rawIncome;

        BigDecimal foirFactor = policy.foirPercent.divide(HUNDRED, MC);
        BigDecimal maxEmi = monthlyIncome.multiply(foirFactor, MC);

        BigDecimal effectiveEmis = opts.getOrDefault("effectiveEmis", safe(req.existingEmis()));
        BigDecimal netEligibleEmi = maxEmi.subtract(effectiveEmis, MC);

        if (netEligibleEmi.compareTo(BigDecimal.ZERO) < 0) {
            eligible = false;
            reasons.add("Existing EMI obligations exceed allowed FOIR capacity");
        }

        int tenure = req.tenureMonths() != null && req.tenureMonths() > 0 ? req.tenureMonths() : 12;
        BigDecimal estimatedLoanAmount = presentValueFromEmi(
                netEligibleEmi.max(BigDecimal.ZERO),
                safe(req.annualInterestRate()),
                tenure
        );

        if (estimatedLoanAmount.compareTo(policy.minimumLoanAmount) < 0) {
            eligible = false;
            reasons.add("Below minimum loan amount threshold");
        }
        if (estimatedLoanAmount.compareTo(policy.maximumLoanAmount) > 0) {
            reasons.add("Capped by maximum loan amount policy");
        }

        BigDecimal policyCapped = estimatedLoanAmount.min(policy.maximumLoanAmount);
        BigDecimal requested = safe(req.requestedLoanAmount());
        BigDecimal finalLoan = requested.compareTo(BigDecimal.ZERO) > 0 ? policyCapped.min(requested) : policyCapped;

        return new EligibilityResult(
                program,
                scale(monthlyIncome), // Returning normalized monthly income for the frontend
                scale(maxEmi),
                scale(netEligibleEmi.max(BigDecimal.ZERO)),
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

    /**
     * 🧠 THE O(1) FINANCIAL MATHEMATICS PROTOCOL
     * Replaces the O(N) CPU-killing loop with the closed-form Present Value of Annuity formula:
     * PV = EMI * [ ((1+r)^n - 1) / (r * (1+r)^n) ]
     */
    private BigDecimal presentValueFromEmi(BigDecimal emi, BigDecimal annualRatePercent, int tenureMonths) {
        if (emi.compareTo(BigDecimal.ZERO) <= 0 || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }

        if (annualRatePercent == null || annualRatePercent.compareTo(BigDecimal.ZERO) == 0) {
            return emi.multiply(BigDecimal.valueOf(tenureMonths), MC);
        }

        BigDecimal monthlyRate = annualRatePercent.divide(new BigDecimal("1200"), MC);

        // (1 + r)^n  -> Extracted to a single highly efficient pow() execution
        BigDecimal onePlusRToTheN = BigDecimal.ONE.add(monthlyRate, MC).pow(tenureMonths, MC);

        // Numerator: (1 + r)^n - 1
        BigDecimal numerator = onePlusRToTheN.subtract(BigDecimal.ONE, MC);

        // Denominator: r * (1 + r)^n
        BigDecimal denominator = monthlyRate.multiply(onePlusRToTheN, MC);

        // PV = EMI * (Numerator / Denominator)
        return emi.multiply(numerator.divide(denominator, MC), MC);
    }

    // 🧠 Safeguards against NPEs when mapping JSON payloads to BigDecimal math
    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static BigDecimal scale(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
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
    ) {}
}
