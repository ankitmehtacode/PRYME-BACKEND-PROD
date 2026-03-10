package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final LoanApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplications() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream().map(ApplicationResponse::from).toList();
    }



    @Transactional(readOnly = true)
    public List<ApplicationResponse> listMyApplications(java.util.UUID applicantId) {
        return applicationRepository.findAllByApplicant_IdOrderByCreatedAtDesc(applicantId).stream()
                .map(ApplicationResponse::from)
                .toList();
    }


    @Transactional
    public ApplicationResponse updateStatus(String applicationId, UpdateStatusRequest request) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validateVersion(application.getVersion(), request.version());

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid status value");
        }

        application.setStatus(newStatus);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponse assign(String applicationId, AssignLeadRequest request) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        validateVersion(application.getVersion(), request.version());

        String normalizedAssignee = request.assigneeId().trim().toUpperCase();
        boolean allowed = Arrays.stream(ApplicationAssignee.values()).map(Enum::name).anyMatch(a -> a.equals(normalizedAssignee));
        if (!allowed) {
            throw new ConflictException("Invalid assigneeId");
        }

        application.setAssignee(normalizedAssignee);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    private static void validateVersion(Long current, Long requestVersion) {
        if (requestVersion == null) {
            return;
        }
        if (current == null || !current.equals(requestVersion)) {
            throw new ConflictException("Version mismatch. Please refresh and retry.");
        }
    }
}
