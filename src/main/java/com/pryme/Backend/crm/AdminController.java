package com.pryme.Backend.crm;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*") // 🧠 Ensures your Vite frontend can talk to this without CORS errors
public class AdminController {

    private final LoanApplicationRepository applicationRepository;

    public AdminController(LoanApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    // 1. Fetch all applications for the CRM Dashboard
    @GetMapping("/applications")
    public ResponseEntity<List<LoanApplication>> getAllApplications() {
        List<LoanApplication> applications = applicationRepository.findAll();
        return ResponseEntity.ok(applications);
    }

    // 2. 🧠 Update Pipeline Status (Triggered by frontend dropdown)
    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable String applicationId, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");

        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Convert string to Enum and save
        application.setStatus(ApplicationStatus.valueOf(newStatus.toUpperCase()));
        applicationRepository.save(application);

        return ResponseEntity.ok(Map.of(
                "message", "Status updated successfully",
                "applicationId", applicationId,
                "status", newStatus
        ));
    }

    // 3. 🧠 Assign Lead to RM (Triggered by frontend dropdown)
    @PatchMapping("/applications/{applicationId}/assign")
    public ResponseEntity<?> assignApplication(@PathVariable String applicationId, @RequestBody Map<String, String> payload) {
        String assigneeId = payload.get("assigneeId");

        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setAssignee(assigneeId);
        applicationRepository.save(application);

        return ResponseEntity.ok(Map.of(
                "message", "Lead assigned successfully",
                "applicationId", applicationId,
                "assignee", assigneeId
        ));
    }
}