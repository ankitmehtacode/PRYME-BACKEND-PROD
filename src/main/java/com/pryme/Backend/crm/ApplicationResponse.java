package com.pryme.Backend.crm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

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

        Map<String, Object> metadata, // 🧠 FIXED: Expose application metadata for frontend hydration

        String assignee,
        String assignedTo, // 🧠 NEW: Assignee UUID for frontend React Select component
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

        // 🧠 PIPELINE FIX: User has both 'phone' and 'phoneNumber' columns. Fall back to ensure data.
        String applicantPhone = app.getApplicant().getPhone() != null
                ? app.getApplicant().getPhone()
                : app.getApplicant().getPhoneNumber();

        // 🧠 Extract Assignee UUID
        String assignedToId = app.getAssignee() == null || app.getAssignee().getId() == null
                ? null
                : app.getAssignee().getId().toString();

        return new ApplicationResponse(
                app.getId() != null ? app.getId().toString() : null,
                app.getApplicationId(),
                // 🧠 FIX: ApplicantSnapshot positional args are (id, name, email, phone, CITY, STATE, metadata)
                // Previously city and state were swapped. Also uses the phone fallback above.
                new ApplicantSnapshot(app.getApplicant().getId(), applicantName, app.getApplicant().getEmail(), applicantPhone, app.getApplicant().getCity(), app.getApplicant().getState(), app.getApplicant().getMetadata()),
                app.getLoanType(),
                app.getRequestedAmount(),
                app.getDeclaredCibilScore(),
                app.getStatus() != null ? app.getStatus().name() : "SUBMITTED",

                // 🧠 FALLBACK INJECTION: If a legacy application lacks this data, default to post-auth state
                app.getCompletionPercentage() != null ? app.getCompletionPercentage() : 50,
                app.getCurrentStep() != null ? app.getCurrentStep() : "COMPLEX_PROFILING",

                app.getMetadata(), // 🧠 FIXED: Re-hydrate dashboard forms correctly

                assigneeName,
                assignedToId,
                app.getCreatedAt(),
                app.getVersion()
        );
    }
}
