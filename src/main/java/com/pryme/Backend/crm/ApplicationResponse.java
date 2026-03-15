package com.pryme.Backend.crm;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 🧠 SILICON-GRADE DTO:
 * Ensures zero data leakage between the database entity and the React frontend.
 * Maps relational objects (like Assignee/Applicant) to flat, secure snapshots.
 */
public record ApplicationResponse(
        String id,
        String applicationId,
        ApplicantSnapshot applicant,
        String loanType,
        BigDecimal requestedAmount,
        Integer declaredCibilScore,
        String status,

        // 🧠 NEW: Progressive Profiling & Lead Salvage Engine
        Integer completionPercentage,
        String currentStep,

        String assignee,
        Instant createdAt, // 🧠 UPGRADED: Standardized to UTC Instant
        Long version
) {
    public static ApplicationResponse from(LoanApplication app) {
        // 🛡️ FAILPROOF EXTRACTOR: Applicant Name
        String applicantName = app.getApplicant() == null || app.getApplicant().getFullName() == null
                ? "Unknown"
                : app.getApplicant().getFullName();

        // 🛡️ FAILPROOF EXTRACTOR: Assignee Name (Prevents Type Mismatch crashes)
        String assigneeName = app.getAssignee() == null || app.getAssignee().getFullName() == null
                ? "UNASSIGNED"
                : app.getAssignee().getFullName();

        return new ApplicationResponse(
                app.getId() != null ? app.getId().toString() : null,
                app.getApplicationId(),
                new ApplicantSnapshot(applicantName),
                app.getLoanType(),
                app.getRequestedAmount(),
                app.getDeclaredCibilScore(),
                app.getStatus() != null ? app.getStatus().name() : "SUBMITTED",

                // 🧠 FALLBACK INJECTION: If a legacy application lacks this data, default to post-auth state
                app.getCompletionPercentage() != null ? app.getCompletionPercentage() : 50,
                app.getCurrentStep() != null ? app.getCurrentStep() : "COMPLEX_PROFILING",

                assigneeName,
                app.getCreatedAt(),
                app.getVersion()
        );
    }
}