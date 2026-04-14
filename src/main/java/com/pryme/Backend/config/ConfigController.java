package com.pryme.Backend.config;

import com.pryme.Backend.bankconfig.BankService;
import com.pryme.Backend.loanproduct.LoanProductType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 🧠 CONFIG CONTROLLER — THE DICTIONARY HYDRATION ENDPOINT
 *
 * This controller backs the frontend's `AppInitializer → useDictionaries.hydrate()` boot sequence.
 * It returns all dropdown/select data from database enums and live bank configs.
 *
 * Contract: GET /api/v1/config/dictionaries
 * Auth:     Required (authenticated users only — falls through to anyRequest().authenticated())
 * Cache:    L1 Caffeine — invalidated on bank CRUD via BankService.surgicallyEvictCaches()
 */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final BankService bankService;

    @Operation(summary = "Hydrate frontend dictionary store with all dropdown/select data")
    @GetMapping("/dictionaries")
    @Cacheable(cacheNames = "config:dictionaries")
    public ResponseEntity<Map<String, Object>> getDictionaries() {

        // 🧠 LOAN TYPES: Sourced from the LoanProductType enum (single source of truth)
        List<Map<String, String>> loanTypes = Arrays.stream(LoanProductType.values())
                .map(t -> Map.of(
                        "value", t.name(),
                        "label", formatEnumLabel(t.name())
                ))
                .collect(Collectors.toList());

        // 🧠 BANK LIST: Sourced from live database — NOT hardcoded
        // Uses the existing cached BankService.getAll() method
        List<Map<String, String>> bankList = bankService.getAll().stream()
                .map(b -> Map.of(
                        "value", b.bankName().toUpperCase().replace(" ", "_"),
                        "label", b.bankName()
                ))
                .collect(Collectors.toList());

        // 🧠 EMPLOYMENT CATEGORIES: Static enum (no DB table yet)
        List<Map<String, String>> employmentCategories = List.of(
                Map.of("value", "SALARIED", "label", "Salaried"),
                Map.of("value", "PROFESSIONAL", "label", "Professional"),
                Map.of("value", "SELF_EMPLOYED", "label", "Self Employed")
        );

        // 🧠 DOCUMENT TYPES: Static enum (matches DocumentVaultController types)
        List<Map<String, String>> documentTypes = List.of(
                Map.of("value", "PAN_CARD", "label", "PAN Card"),
                Map.of("value", "AADHAR", "label", "Aadhaar Card"),
                Map.of("value", "ITR", "label", "Income Tax Return"),
                Map.of("value", "BANK_STATEMENT", "label", "Bank Statement"),
                Map.of("value", "SALARY_SLIP", "label", "Salary Slip"),
                Map.of("value", "FORM_16", "label", "Form 16"),
                Map.of("value", "PROPERTY_PAPERS", "label", "Property Papers"),
                Map.of("value", "BUSINESS_PROOF", "label", "Business Proof")
        );

        // 🧠 PROPERTY TYPES: Static enum
        List<Map<String, String>> propertyTypes = List.of(
                Map.of("value", "FLAT", "label", "Flat / Apartment"),
                Map.of("value", "HOUSE", "label", "Independent House"),
                Map.of("value", "PLOT", "label", "Plot"),
                Map.of("value", "COMMERCIAL", "label", "Commercial Property")
        );

        // 🧠 STATE LIST: Indian states for address forms
        List<Map<String, String>> states = List.of(
                Map.of("value", "MH", "label", "Maharashtra"),
                Map.of("value", "DL", "label", "Delhi"),
                Map.of("value", "KA", "label", "Karnataka"),
                Map.of("value", "TN", "label", "Tamil Nadu"),
                Map.of("value", "GJ", "label", "Gujarat"),
                Map.of("value", "RJ", "label", "Rajasthan"),
                Map.of("value", "UP", "label", "Uttar Pradesh"),
                Map.of("value", "WB", "label", "West Bengal"),
                Map.of("value", "AP", "label", "Andhra Pradesh"),
                Map.of("value", "TS", "label", "Telangana"),
                Map.of("value", "KL", "label", "Kerala"),
                Map.of("value", "PB", "label", "Punjab"),
                Map.of("value", "HR", "label", "Haryana"),
                Map.of("value", "MP", "label", "Madhya Pradesh"),
                Map.of("value", "BR", "label", "Bihar"),
                Map.of("value", "GA", "label", "Goa")
        );

        Map<String, Object> dictionaries = new LinkedHashMap<>();
        dictionaries.put("loanTypes", loanTypes);
        dictionaries.put("bankList", bankList);
        dictionaries.put("employmentCategories", employmentCategories);
        dictionaries.put("documentTypes", documentTypes);
        dictionaries.put("propertyTypes", propertyTypes);
        dictionaries.put("states", states);

        return ResponseEntity.ok(dictionaries);
    }

    /**
     * Converts "HOME_LOAN" → "Home Loan", "PERSONAL" → "Personal"
     */
    private String formatEnumLabel(String enumName) {
        return Arrays.stream(enumName.split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
