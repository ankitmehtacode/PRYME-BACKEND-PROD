package com.pryme.Backend.eligibility;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eligibility")
@CrossOrigin(origins = "*") // Allows your frontend to connect
public class EligibilityController {

    // L&T Finance Normal Income Program (NIP)
    @PostMapping("/lnt-lap/nip")
    public Map<String, Object> calculateNipEligibility(@RequestBody NipRequest request) {
        // Gross = PAT + Depreciation + Interest
        BigDecimal grossIncome = request.pat().add(request.depreciation()).add(request.interest());
        // FOIR is 95% (0.95)
        BigDecimal maxEmiAllowed = grossIncome.multiply(new BigDecimal("0.95"));
        BigDecimal actualEligibleEmi = maxEmiAllowed.subtract(request.existingEmis());

        return Map.of(
                "bank", "L&T Finance",
                "program", "LAP - Normal Income",
                "grossIncomeCalculated", grossIncome,
                "maxEligibleEmi", actualEligibleEmi.max(BigDecimal.ZERO)
        );
    }

    // L&T Finance GST Surrogate Program
    @PostMapping("/lnt-lap/gst")
    public Map<String, Object> calculateGstEligibility(@RequestBody GstRequest request) {
        BigDecimal profitMargin = switch(request.businessType().toUpperCase()) {
            case "SERVICE" -> new BigDecimal("0.10"); // 10%
            case "RETAILER" -> new BigDecimal("0.12"); // 12%
            case "WHOLESALER" -> new BigDecimal("0.08"); // 8%
            case "MANUFACTURER" -> new BigDecimal("0.04"); // 4%
            default -> BigDecimal.ZERO;
        };

        BigDecimal estimatedIncome = request.turnover12M().multiply(profitMargin);
        BigDecimal maxEmiAllowed = estimatedIncome.multiply(new BigDecimal("0.65")); // 65% FOIR for GST
        BigDecimal actualEligibleEmi = maxEmiAllowed.subtract(request.existingEmis());

        return Map.of(
                "bank", "L&T Finance",
                "program", "LAP - GST Surrogate",
                "estimatedIncome", estimatedIncome,
                "maxEligibleEmi", actualEligibleEmi.max(BigDecimal.ZERO)
        );
    }
}

// Data Transfer Objects (DTOs)
record NipRequest(BigDecimal pat, BigDecimal depreciation, BigDecimal interest, BigDecimal existingEmis) {}
record GstRequest(BigDecimal turnover12M, String businessType, BigDecimal existingEmis) {}