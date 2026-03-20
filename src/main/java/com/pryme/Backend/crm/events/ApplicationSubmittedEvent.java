package com.pryme.Backend.crm.events;

import com.pryme.Backend.crm.ApplicantSnapshot;

import java.math.BigDecimal;

/**
 * 🧠 CROSS-DOMAIN EVENT RECORD
 * Fired by the CRM module when a user officially submits their multi-stage application.
 * Consumed asynchronously by the Eligibility/Underwriting Engine.
 */
public record ApplicationSubmittedEvent(
        String applicationId,
        String loanType,
        String selectedBank,
        BigDecimal requestedAmount,
        ApplicantSnapshot applicantSnapshot
) {}