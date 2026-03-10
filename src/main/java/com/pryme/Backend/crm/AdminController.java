package com.pryme.Backend.crm;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplicationService applicationService;

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.listApplications());
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<Map<String, Object>> updateApplicationStatus(
            @PathVariable String applicationId,
            @Valid @RequestBody UpdateStatusRequest payload
    ) {
        ApplicationResponse updated = applicationService.updateStatus(applicationId, payload);

        return ResponseEntity.ok(Map.of(
                "message", "Status updated successfully",
                "application", updated
        ));
    }

    @PatchMapping("/applications/{applicationId}/assign")
    public ResponseEntity<Map<String, Object>> assignApplication(
            @PathVariable String applicationId,
            @Valid @RequestBody AssignLeadRequest payload
    ) {
        ApplicationResponse updated = applicationService.assign(applicationId, payload);

        return ResponseEntity.ok(Map.of(
                "message", "Lead assigned successfully",
                "application", updated
        ));
    }
}
