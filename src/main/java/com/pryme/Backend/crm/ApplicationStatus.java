package com.pryme.Backend.crm;

public enum ApplicationStatus {
    DRAFT,          // 🧠 Added: User has started the flow but hasn't finalized bank selection
    SUBMITTED,
    PROCESSING,
    VERIFIED,
    APPROVED,
    REJECTED
}