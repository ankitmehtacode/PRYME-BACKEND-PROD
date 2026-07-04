package com.pryme.Backend.eligibility.audit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AuditComparisonResult(
    DecisionTrace trace,
    AuditComparisonRequest.ExpectedResult expected,
    ActualResult actual,
    List<Mismatch> mismatches,
    boolean pass
) {
    public record ActualResult(
        String program,
        BigDecimal amount,
        BigDecimal foir,
        BigDecimal ltv,
        BigDecimal roi,
        BigDecimal income,
        boolean eligible
    ) {}

    public record Mismatch(
        String field,
        Object expected,
        Object actual,
        BigDecimal deviation
    ) {}

    public static AuditComparisonResult compare(DecisionTrace trace, AuditComparisonRequest.ExpectedResult expected) {
        if (trace == null || expected == null) {
            throw new IllegalArgumentException("Trace and expected result must not be null");
        }

        DecisionSummary summary = trace.summary();
        boolean isEligible = summary.finalStatus() == DecisionStatus.PASS;
        String actualProgram = summary.selectedProgram() != null ? summary.selectedProgram().name() : null;
        BigDecimal actualAmount = summary.finalEligibleAmount() != null ? summary.finalEligibleAmount() : BigDecimal.ZERO;
        BigDecimal actualRoi = summary.finalRoi() != null ? summary.finalRoi() : BigDecimal.ZERO;
        BigDecimal actualLtv = summary.finalLtv() != null ? summary.finalLtv() : BigDecimal.ZERO;

        // Extract FOIR and Income from selected step or default to zero
        BigDecimal actualFoir = BigDecimal.ZERO;
        BigDecimal actualIncome = BigDecimal.ZERO;

        if (summary.selectedProgram() != null) {
            for (DecisionStep step : trace.steps()) {
                if (step.program() == summary.selectedProgram()) {
                    if (step.effectiveFoir() != null) {
                        actualFoir = step.effectiveFoir();
                    }
                    if (step.computedIncome() != null) {
                        actualIncome = step.computedIncome();
                    }
                    break;
                }
            }
        }

        ActualResult actual = new ActualResult(
            actualProgram,
            actualAmount,
            actualFoir,
            actualLtv,
            actualRoi,
            actualIncome,
            isEligible
        );

        List<Mismatch> mismatches = new ArrayList<>();

        // 1. Eligible status
        if (expected.expectedEligible() != null) {
            if (expected.expectedEligible() != isEligible) {
                mismatches.add(new Mismatch("ELIGIBLE", expected.expectedEligible(), isEligible, null));
            }
        }

        // 2. Program Name
        if (expected.expectedProgram() != null && !expected.expectedProgram().isBlank()) {
            if (actualProgram == null || !expected.expectedProgram().equalsIgnoreCase(actualProgram)) {
                mismatches.add(new Mismatch("PROGRAM", expected.expectedProgram(), actualProgram, null));
            }
        }

        // 3. Eligible Amount (tolerance of 1.0)
        if (expected.expectedAmount() != null) {
            BigDecimal diff = expected.expectedAmount().subtract(actualAmount).abs();
            if (diff.compareTo(BigDecimal.ONE) > 0) {
                mismatches.add(new Mismatch("ELIGIBLE_AMOUNT", expected.expectedAmount(), actualAmount, diff));
            }
        }

        // 4. ROI (tolerance of 0.0001 or 0.01%)
        if (expected.expectedRoi() != null) {
            BigDecimal diff = expected.expectedRoi().subtract(actualRoi).abs();
            if (diff.compareTo(new BigDecimal("0.0001")) > 0) {
                mismatches.add(new Mismatch("ROI", expected.expectedRoi(), actualRoi, diff));
            }
        }

        // 5. LTV (tolerance of 0.001 or 0.1%)
        if (expected.expectedLtv() != null) {
            BigDecimal diff = expected.expectedLtv().subtract(actualLtv).abs();
            if (diff.compareTo(new BigDecimal("0.001")) > 0) {
                mismatches.add(new Mismatch("LTV", expected.expectedLtv(), actualLtv, diff));
            }
        }

        // 6. FOIR (tolerance of 0.001 or 0.1%)
        if (expected.expectedFoir() != null) {
            BigDecimal diff = expected.expectedFoir().subtract(actualFoir).abs();
            if (diff.compareTo(new BigDecimal("0.001")) > 0) {
                mismatches.add(new Mismatch("FOIR", expected.expectedFoir(), actualFoir, diff));
            }
        }

        // 7. Income (tolerance of 1.0)
        if (expected.expectedIncome() != null) {
            BigDecimal diff = expected.expectedIncome().subtract(actualIncome).abs();
            if (diff.compareTo(BigDecimal.ONE) > 0) {
                mismatches.add(new Mismatch("INCOME", expected.expectedIncome(), actualIncome, diff));
            }
        }

        boolean pass = mismatches.isEmpty();
        return new AuditComparisonResult(trace, expected, actual, mismatches, pass);
    }
}
