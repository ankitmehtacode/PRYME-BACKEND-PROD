package com.pryme.Backend.bankconfig;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @Operation(summary = "One-line description of this endpoint")
    @PostMapping
    public ResponseEntity<BankResponse> createBank(@Valid @RequestBody BankRequest request) {
        return ResponseEntity.ok(bankService.create(request));
    }

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping
    public ResponseEntity<List<BankResponse>> getAllBanks() {
        return ResponseEntity.ok(bankService.getAll());
    }

    @Operation(summary = "One-line description of this endpoint")
    @PutMapping("/{id}")
    public ResponseEntity<BankResponse> updateBank(@PathVariable UUID id, @Valid @RequestBody BankRequest request) {
        return ResponseEntity.ok(bankService.update(id, request));
    }

    @Operation(summary = "One-line description of this endpoint")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<BankResponse> toggleVisibility(@PathVariable UUID id,
            @RequestBody Map<String, Boolean> payload) {
        boolean active = Boolean.TRUE.equals(payload.get("active"));
        return ResponseEntity.ok(bankService.toggleVisibility(id, active));
    }

    @Operation(summary = "One-line description of this endpoint")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable UUID id) {
        bankService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
