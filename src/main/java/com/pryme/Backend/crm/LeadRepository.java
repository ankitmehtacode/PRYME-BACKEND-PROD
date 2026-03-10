package com.pryme.Backend.crm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    Optional<Lead> findByIdempotencyKey(String idempotencyKey);

    Optional<Lead> findTopByPhoneAndLoanAmountAndLoanTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            String phone,
            BigDecimal loanAmount,
            String loanType,
            LocalDateTime createdAtAfter
    );
}
