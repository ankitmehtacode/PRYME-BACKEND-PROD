package com.pryme.Backend.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    // ==========================================
    // 🧠 1. THE N+1 QUERY ELIMINATOR (EntityGraphs)
    // Whenever we fetch an application by its exact PRYME-ID, we almost always need the User details.
    // @EntityGraph forces Hibernate to do a single SQL JOIN underneath, preventing the N+1 avalanche.
    // ==========================================
    @EntityGraph(attributePaths = {"applicant"})
    Optional<LoanApplication> findByApplicationId(String applicationId);

    // ==========================================
    // 🧠 2. STRICT RELATIONSHIP TRAVERSAL
    // Fixed the missing underscore. 'applicant_Id' correctly tells Spring Data
    // to navigate into the 'User' entity and check its 'id' field.
    // ==========================================
    List<LoanApplication> findByApplicant_IdAndStatus(UUID applicantId, ApplicationStatus status);

    // ==========================================
    // 🧠 3. ELASTIC PAGINATION (OOM PREVENTION)
    // We eradicated the old List<LoanApplication> methods.
    // By forcing Pageable, we guarantee the JVM will never load more than
    // the requested chunk (e.g., 20) into RAM, even if the user has 1,000 historical drafts.
    // ==========================================
    @EntityGraph(attributePaths = {"applicant"})
    Page<LoanApplication> findAllByApplicant_Id(UUID applicantId, Pageable pageable);

    // NOTE: For the Master Admin Dashboard, the ApplicationService now uses
    // the native `findAll(Pageable)` provided out-of-the-box by JpaRepository.
    // We override it here purely to inject the @EntityGraph, eliminating N+1s on the admin panel!
    @Override
    @EntityGraph(attributePaths = {"applicant", "assignee"})
    Page<LoanApplication> findAll(Pageable pageable);
}