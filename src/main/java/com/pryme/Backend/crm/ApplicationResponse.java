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

        return new ApplicationResponse(
                app.getId().toString(),
                app.getApplicationId(),
                new ApplicantSnapshot(applicantName),
                app.getLoanType(),
                app.getRequestedAmount(),
                app.getDeclaredCibilScore(),
                app.getStatus() == null ? null : app.getStatus().name(),
                app.getAssignee(),
                app.getCreatedAt(),
                app.getVersion()
        );
    }
}
