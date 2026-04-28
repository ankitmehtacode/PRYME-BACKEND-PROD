package com.pryme.Backend.crm;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.common.ForbiddenException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ElevationController {

    private final LeadElevationService elevationService;

    /**
     * 🧠 SECURITY FIX: userId is now extracted from the server-side Authentication
     * principal — NOT from the request body. This closes the BOLA vulnerability
     * where any authenticated user could claim another user's anonymous lead.
     */
    @Operation(summary = "Elevate an anonymous lead to a secure loan application")
    @PostMapping("/elevate")
    public ResponseEntity<Map<String, Object>> elevateLead(
            @Valid @RequestBody ElevateRequest request,
            Authentication authentication
    ) {
        UUID userId = extractUserId(authentication);

        ApplicationResponse elevatedApp = elevationService.elevate(
                request.leadId(), userId, request.selectedBank());

        return ResponseEntity.ok(Map.of(
                "code", "SUCCESS",
                "message", "Lead successfully elevated to secure Loan Application",
                "application", elevatedApp
        ));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
            throw new ForbiddenException("Authentication required to elevate a lead.");
        }
        return (UUID) authentication.getPrincipal();
    }
}