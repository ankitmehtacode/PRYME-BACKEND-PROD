package com.pryme.Backend.bankconfig;

import java.util.UUID;

public record PartnerBankResponse(
        UUID id,
        String name,
        String logo
) {
    public static PartnerBankResponse from(Bank bank) {
        return new PartnerBankResponse(bank.getId(), bank.getBankName(), bank.getLogoUrl());
    }
}
