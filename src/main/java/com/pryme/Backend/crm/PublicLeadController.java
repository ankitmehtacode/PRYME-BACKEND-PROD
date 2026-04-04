package com.pryme.Backend.crm;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public") // Supports optional Idempotency-Key header for POST /leads
@RequiredArgsConstructor
public class PublicLeadController {

    private final LeadService leadService;

    @PostMapping("/leads")
    public ResponseEntity<Map<String, Object>> submitLead(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody LeadSubmitRequest request
    ) {
        LeadResponse response = leadService.submitLead(request, idempotencyKey);
        return ResponseEntity.ok(Map.of(
                "message", "Lead submitted successfully",
                "lead", response
        ));
    }
}
