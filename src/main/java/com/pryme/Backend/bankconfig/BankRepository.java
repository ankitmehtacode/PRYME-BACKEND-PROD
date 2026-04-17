package com.pryme.Backend.bankconfig;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {
    boolean existsByBankNameIgnoreCase(String bankName);

    java.util.List<Bank> findTop15ByActiveTrueOrderByBankNameAsc();
}
