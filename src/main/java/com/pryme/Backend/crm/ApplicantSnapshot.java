package com.pryme.Backend.crm;

import java.util.Map;
import java.util.UUID;

/**
 * 🧠 EVENT-CARRIED STATE TRANSFER (ECST)
 * This immutable record captures the exact state of the applicant at the moment of submission.
 * It prevents downstream domains (like Eligibility) from having to query the database again.
 */
public record ApplicantSnapshot(
        UUID applicantId,
        String fullName,
        String email,
        String phoneNumber,
        String city,
        String state,
        // Passes the entire dynamic Zustand payload (Employment Type, Sub-Types, etc.)
        Map<String, Object> applicationMetadata
) {
    // 🧠 Factory method for clean instantiation from the CRM Service
    public static ApplicantSnapshot from(LoanApplication application) {
        return new ApplicantSnapshot(
                application.getApplicant().getId(),
                application.getApplicant().getFullName(),
                application.getApplicant().getEmail(),
                application.getApplicant().getPhoneNumber(),
                application.getApplicant().getCity(),
                application.getApplicant().getState(),
                // Defensive copy of metadata to ensure true immutability in the event stream
                application.getMetadata() != null ? Map.copyOf(application.getMetadata()) : Map.of()
        );
    }
}