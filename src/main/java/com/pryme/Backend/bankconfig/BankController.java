package com.pryme.Backend.bankconfig;

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

    @PostMapping
    public ResponseEntity<BankResponse> createBank(@Valid @RequestBody BankRequest request) {
        return ResponseEntity.ok(bankService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BankResponse>> getAllBanks() {
        return ResponseEntity.ok(bankService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BankResponse> updateBank(@PathVariable UUID id, @Valid @RequestBody BankRequest request) {
        return ResponseEntity.ok(bankService.update(id, request));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<BankResponse> toggleVisibility(@PathVariable UUID id, @RequestBody Map<String, Boolean> payload) {
        boolean active = Boolean.TRUE.equals(payload.get("active"));
        return ResponseEntity.ok(bankService.toggleVisibility(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable UUID id) {
        bankService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
