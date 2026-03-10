package com.pryme.Backend.crm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        String userName,
        String phone,
        BigDecimal loanAmount,
        String loanType,
        String status,
        String offerId,
        LocalDateTime createdAt
) {
    public static LeadResponse from(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getUserName(),
                lead.getPhone(),
                lead.getLoanAmount(),
                lead.getLoanType(),
                lead.getStatus().name(),
                lead.getOfferId(),
                lead.getCreatedAt()
        );
    }
}
