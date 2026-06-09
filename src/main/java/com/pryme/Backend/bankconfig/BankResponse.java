package com.pryme.Backend.bankconfig;

import java.util.UUID;

public record BankResponse(
        UUID id,
        String bankName,
        String logoUrl,
        boolean active,
        Long lenderCode
) {
    public static BankResponse from(Bank bank) {
        return new BankResponse(bank.getId(), bank.getBankName(), bank.getLogoUrl(), bank.isActive(), bank.getLenderCode());
    }
}
