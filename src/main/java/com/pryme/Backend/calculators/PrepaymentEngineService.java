package com.pryme.Backend.calculators;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class PrepaymentEngineService {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE_HUNDRED = new BigDecimal("1200");
    private static final BigDecimal STEP_UP = new BigDecimal("1.05");

    public PrepaymentRoiResponse calculateRoi(PrepaymentRoiRequest request) {
        BigDecimal emi = computeEmi(request.principalAmount(), request.annualInterestRate(), request.tenureMonths());

        Simulation baseline = simulate(request.principalAmount(), request.annualInterestRate(), request.tenureMonths(), emi,
                request.prepaymentAmount(), false, false);

        StrategyImpact s1 = impact("13th-emi", "The 13th EMI", "Pay just 1 extra EMI every year.", baseline,
                simulate(request.principalAmount(), request.annualInterestRate(), request.tenureMonths(), emi,
                        request.prepaymentAmount(), true, false));

        StrategyImpact s2 = impact("5-percent", "5% Step-Up", "Increase your EMI by 5% annually.", baseline,
                simulate(request.principalAmount(), request.annualInterestRate(), request.tenureMonths(), emi,
                        request.prepaymentAmount(), false, true));

        StrategyImpact s3 = impact("combo", "PRYME Combo", "13th EMI + 5% Annual Step-Up.", baseline,
                simulate(request.principalAmount(), request.annualInterestRate(), request.tenureMonths(), emi,
                        request.prepaymentAmount(), true, true));

        List<StrategyImpact> impacts = List.of(s1, s2, s3);
        StrategyImpact best = impacts.stream().max(Comparator.comparing(StrategyImpact::interestSaved)).orElse(s1);

        List<StrategyImpact> marked = impacts.stream().map(i -> new StrategyImpact(
                i.id(), i.name(), i.description(), i.interestSaved(), i.timeTrimmedMonths(), i.optimizedTotalInterest(), i.id().equals(best.id())
        )).toList();

        return new PrepaymentRoiResponse(
                scale(request.principalAmount()),
                scale(request.annualInterestRate()),
                request.tenureMonths(),
                scale(baseline.totalInterest()),
                marked
        );
    }

    private StrategyImpact impact(String id, String name, String description, Simulation baseline, Simulation optimized) {
        BigDecimal saved = baseline.totalInterest().subtract(optimized.totalInterest(), MC).max(BigDecimal.ZERO);
        int timeSaved = Math.max(0, baseline.monthsTaken() - optimized.monthsTaken());
        return new StrategyImpact(id, name, description, scale(saved), timeSaved, scale(optimized.totalInterest()), false);
    }

    private Simulation simulate(BigDecimal principal, BigDecimal annualRate, int tenureMonths, BigDecimal baseEmi,
                                BigDecimal annualPrepayment, boolean useExtraAnnualEmi, boolean useStepUp) {
        BigDecimal monthlyRate = annualRate.divide(TWELVE_HUNDRED, MC);
        BigDecimal balance = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal emi = baseEmi;

        int month = 0;
        while (balance.compareTo(BigDecimal.ZERO) > 0 && month < 1000) {
            month++;

            BigDecimal interest = balance.multiply(monthlyRate, MC);
            BigDecimal principalComponent = emi.subtract(interest, MC);
            if (principalComponent.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("EMI too low to amortize loan");
            }

            if (principalComponent.compareTo(balance) > 0) {
                principalComponent = balance;
            }

            balance = balance.subtract(principalComponent, MC);
            totalInterest = totalInterest.add(interest, MC);

            if (month % 12 == 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
                if (useStepUp) {
                    emi = emi.multiply(STEP_UP, MC);
                }

                BigDecimal annualExtra = useExtraAnnualEmi ? annualPrepayment.max(baseEmi) : annualPrepayment;
                BigDecimal extra = annualExtra.min(balance);
                balance = balance.subtract(extra, MC);
            }

            if (month > tenureMonths * 3 && balance.compareTo(BigDecimal.ZERO) > 0) {
                break;
            }
        }

        return new Simulation(totalInterest.max(BigDecimal.ZERO), month);
    }

    private BigDecimal computeEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        BigDecimal monthlyRate = annualRatePercent.divide(TWELVE_HUNDRED, MC);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), MC);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal growth = onePlusR.pow(tenureMonths, MC);
        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(growth, MC);
        BigDecimal denominator = growth.subtract(BigDecimal.ONE, MC);

        return numerator.divide(denominator, MC);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    private record Simulation(BigDecimal totalInterest, int monthsTaken) {
    }
}
