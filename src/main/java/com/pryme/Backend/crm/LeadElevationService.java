package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadElevationService {

    private final LeadRepository leadRepository;
    private final LoanApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApplicationResponse elevate(UUID leadId, UUID userId) {

        // 1. Validate the public lead exists
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead identity not found in the public matrix."));

        // 2. 🧠 ZERO-COMPROMISE SECURITY: Prevent double-elevation attacks
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new ConflictException("Security Violation: This lead has already been converted to an active application.");
        }

        // 3. Validate the authenticated user exists
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Authenticated User identity not found. Cannot bind application."));

        // 4. Generate sequential CRM Application ID
        String generateAppId = "PRY-" + (10000 + applicationRepository.count() + 1);

        // 5. Fuse the data into the highly-secure LoanApplication entity
        LoanApplication application = LoanApplication.builder()
                .applicationId(generateAppId)
                .applicant(applicant)
                .loanType(lead.getLoanType())
                .requestedAmount(lead.getLoanAmount())
                .declaredCibilScore(lead.getCibilScore() != null ? lead.getCibilScore() : 0)
                .status(ApplicationStatus.SUBMITTED) // Places it natively into the Admin Kanban board
                .build();

        application = applicationRepository.save(application);

        // 6. Lock the public lead so it cannot be tampered with again
        lead.setStatus(LeadStatus.CONVERTED);
        leadRepository.save(lead);

        return ApplicationResponse.from(application);
    }
}