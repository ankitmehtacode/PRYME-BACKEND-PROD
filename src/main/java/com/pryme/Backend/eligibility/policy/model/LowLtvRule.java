package com.pryme.Backend.eligibility.policy.model;

import java.math.BigDecimal;

public record LowLtvRule(
    String loanType,          // "HL" or "LAP"
    String lenderName,        // LAP specific
    String propertyCategory,  // LAP specific (e.g., "Residential", "Commercial")
    String propertyType,      // HL propertyType or LAP propertySubtype (e.g., "Ready Built Property", "Plot")
    BigDecimal minLoanAmount, // HL specific
    BigDecimal maxLoanAmount, // HL specific
    String ltvValue           // Value like "0.75" or "Negative"
) {}
