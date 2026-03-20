package com.pryme.Backend.crm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {
    Optional<LoanApplication> findByApplicationId(String applicationId);

    List<LoanApplication> findAllByOrderByCreatedAtDesc();
    List<LoanApplication> findByApplicantIdAndStatus(UUID applicantId, ApplicationStatus status);
    List<LoanApplication> findAllByApplicant_IdOrderByCreatedAtDesc(UUID applicantId);
}
