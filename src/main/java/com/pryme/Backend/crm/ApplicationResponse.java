package com.pryme.Backend.crm;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApplicationResponse(
        String id,
        String applicationId,
        ApplicantSnapshot applicant,
        String loanType,
        BigDecimal requestedAmount,
        Integer declaredCibilScore,
        String status,
        String assignee,
        LocalDateTime createdAt,
        Long version
) {
    public static ApplicationResponse from(LoanApplication app) {
        String applicantName = app.getApplicant() == null || app.getApplicant().getFullName() == null
                ? "Unknown"
                : app.getApplicant().getFullName();

        // 🧠 PRODUCTION FIX: Safely unpack the Relational User Entity into a String
        String assigneeName = app.getAssignee() == null || app.getAssignee().getFullName() == null
                ? "UNASSIGNED"
                : app.getAssignee().getFullName();

        return new ApplicationResponse(
                app.getId().toString(),
                app.getApplicationId(),
                new ApplicantSnapshot(applicantName),
                app.getLoanType(),
                app.getRequestedAmount(),
                app.getDeclaredCibilScore(),
                app.getStatus() == null ? null : app.getStatus().name(),
                assigneeName, // Pass the safely extracted String, preventing the Type Mismatch
                app.getCreatedAt(),
                app.getVersion()
        );
    }
}