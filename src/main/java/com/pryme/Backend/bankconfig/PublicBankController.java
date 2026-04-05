package com.pryme.Backend.bankconfig;

import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/banks")
@RequiredArgsConstructor
public class PublicBankController {

    private final BankService bankService;

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping("/partners")
    public ResponseEntity<Map<String, List<PartnerBankResponse>>> partners() {
        return ResponseEntity.ok(Map.of("banks", bankService.getActivePartners()));
    }
}
