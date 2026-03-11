package com.pryme.Backend.crm;

import com.pryme.Backend.common.ForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponse>> myApplications(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
            throw new ForbiddenException("Authentication required");
        }
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(applicationService.listMyApplications(userId));
    }
}
