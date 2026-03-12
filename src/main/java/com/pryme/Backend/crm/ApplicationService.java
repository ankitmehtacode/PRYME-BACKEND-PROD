package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final LoanApplicationRepository applicationRepository;
    // 🧠 PRODUCTION FIX: Inject UserRepository to validate dynamic employees, not hardcoded Enums
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplications() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream().map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listMyApplications(UUID applicantId) {
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

        // 🧠 PRODUCTION FIX: Safely parse the Assignee ID and fetch the actual Relational User Entity
        UUID empId;
        try {
            empId = UUID.fromString(request.assigneeId().trim());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Invalid assigneeId format. Expected a valid User UUID.");
        }

        User employee = userRepository.findById(empId)
                .orElseThrow(() -> new NotFoundException("Assignee not found in IAM system records."));

        // Successfully pass the User Entity, resolving the compilation error
        application.setAssignee(employee);

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