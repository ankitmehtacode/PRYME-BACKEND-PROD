package com.pryme.Backend.crm;

import java.math.BigDecimal;
import java.util.Map;

public record UpdateApplicationProfileRequest(
        String fullName,
        String phone,
        String email,
        String state,
        String city,
        String loanType,
        BigDecimal requestedAmount,
        @com.pryme.Backend.common.validation.ValidCibilScore Integer declaredCibilScore,
        Map<String, Object> metadata
) {
}
