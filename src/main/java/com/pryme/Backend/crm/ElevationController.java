package com.pryme.Backend.crm;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ElevationController {

    private final LeadElevationService elevationService;

    @Operation(summary = "One-line description of this endpoint")
    @PostMapping("/elevate")
    public ResponseEntity<Map<String, Object>> elevateLead(
            @Valid @RequestBody ElevateRequest request
    ) {
        ApplicationResponse elevatedApp = elevationService.elevate(request.leadId(), request.userId(), request.selectedBank());

        return ResponseEntity.ok(Map.of(
                "code", "SUCCESS",
                "message", "Lead successfully elevated to secure Loan Application",
                "application", elevatedApp
        ));
    }
}