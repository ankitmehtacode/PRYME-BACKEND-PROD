package com.pryme.Backend.calculators;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/calculators/prepayment")
@CrossOrigin(origins = "*")
public class PrepaymentEngine {

    // Engine who collects Input with 4 fields
    @PostMapping("/analyze")
    public Map<String, Object> calculatePrepaymentStrategy(@RequestBody PrepaymentRequest request) {

        // Strategy 1: 13th EMI Logic (Paying one extra EMI per year)
        int monthsSavedWith13thEmi = calculateMonthsSaved(request, true, false);

        // Strategy 2: 5% increment on EMI every year
        int monthsSavedWith5Percent = calculateMonthsSaved(request, false, true);

        // Strategy 3: The combination of 13th EMI and 5% increment
        int monthsSavedHybrid = calculateMonthsSaved(request, true, true);

        return Map.of(
                "originalTenureMonths", request.tenureMonths(),
                "monthsSavedStrategy1_13thEmi", monthsSavedWith13thEmi,
                "monthsSavedStrategy2_5PercentInc", monthsSavedWith5Percent,
                "monthsSavedStrategy3_Hybrid", monthsSavedHybrid
        );
    }

    private int calculateMonthsSaved(PrepaymentRequest req, boolean use13thEmi, boolean use5PercentInc) {
        BigDecimal balance = req.principalAmount();
        BigDecimal monthlyRate = req.annualInterestRate().divide(new BigDecimal("1200"), 8, RoundingMode.HALF_UP);
        BigDecimal currentEmi = req.currentEmi();

        int monthCount = 0;

        while (balance.compareTo(BigDecimal.ZERO) > 0 && monthCount < req.tenureMonths()) {
            monthCount++;

            // Add monthly interest
            BigDecimal interestForMonth = balance.multiply(monthlyRate);
            balance = balance.add(interestForMonth);

            // Pay EMI
            balance = balance.subtract(currentEmi);

            // Apply 5% increment every 12 months
            if (use5PercentInc && monthCount % 12 == 0) {
                currentEmi = currentEmi.multiply(new BigDecimal("1.05"));
            }

            // Apply 13th EMI (Extra payment once a year)
            if (use13thEmi && monthCount % 12 == 0) {
                balance = balance.subtract(currentEmi);
            }
        }

        return Math.max(0, req.tenureMonths() - monthCount);
    }
}

// The 4 fields required by the Scope of Work
record PrepaymentRequest(
        BigDecimal principalAmount,
        BigDecimal annualInterestRate,
        int tenureMonths,
        BigDecimal currentEmi
)
{

}