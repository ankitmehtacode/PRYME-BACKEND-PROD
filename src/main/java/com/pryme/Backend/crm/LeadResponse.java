package com.pryme.Backend.crm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 🧠 ENRICHED LEAD DTO:
 * Now includes assigneeName alongside assignedTo UUID so the frontend
 * can render "Assigned to: John Doe" in the CRM pipeline without
 * requiring a second lookup call.
 */
public record LeadResponse(
        UUID id,
        String userName,
        String phone,
        BigDecimal loanAmount,
        String loanType,
        String status,
        String offerId,
        LocalDateTime createdAt,
        UUID assignedTo,
        String assigneeName,
        String metadata
) {
    /**
     * Factory for simple mapping (no assignee name resolution).
     * Defaults to "UNASSIGNED" when no assignee is set.
     */
    public static LeadResponse from(Lead lead) {
        return from(lead, null);
    }

    /**
     * Factory with explicit assignee name resolution.
     * Used by LeadService after batch-fetching assignee names.
     */
    public static LeadResponse from(Lead lead, String resolvedAssigneeName) {
        String displayName = lead.getAssignedTo() == null
                ? "UNASSIGNED"
                : (resolvedAssigneeName != null ? resolvedAssigneeName : "Unknown");

        return new LeadResponse(
                lead.getId(),
                lead.getUserName(),
                lead.getPhone(),
                lead.getLoanAmount(),
                lead.getLoanType(),
                lead.getStatus().name(),
                lead.getOfferId(),
                lead.getCreatedAt(),
                lead.getAssignedTo(),
                displayName,
                lead.getMetadata()
        );
    }
}
