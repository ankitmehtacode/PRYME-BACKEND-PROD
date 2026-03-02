package com.pryme.Backend.crm;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {
    // 🧠 This specific query method allows us to search by "PRY-XXXX" instead of the UUID
    Optional<LoanApplication> findByApplicationId(String applicationId);
}