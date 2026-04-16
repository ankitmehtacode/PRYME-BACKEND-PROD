package com.pryme.Backend.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryme.Backend.common.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {

    // 🧠 PRODUCTION FIX: Aligned with frontend product catalog
    private static final Set<String> ALLOWED_LOAN_TYPES = Set.of("personal", "business", "home", "education", "lap");

    // 🧠 PRODUCTION FIX: Static, thread-safe mapper prevents memory leaks during high-volume JSON serialization
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final LeadRepository leadRepository;
    private final LeadBackupService leadBackupService;

    @Transactional
    public LeadResponse submitLead(LeadSubmitRequest request, String idempotencyKey) {
        UUID opId = leadBackupService.begin(request, idempotencyKey);

        String normalizedLoanType = normalizeLoanType(request.loanType());

        LeadResponse result;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String normalizedKey = normalizeKey(idempotencyKey);
            result = leadRepository.findByIdempotencyKey(normalizedKey)
                    .map(LeadResponse::from)
                    .orElseGet(() -> saveLead(request, normalizedLoanType, normalizedKey));
        } else {
            LocalDateTime duplicateWindow = LocalDateTime.now().minusHours(24);
            result = leadRepository.findTopByPhoneAndLoanAmountAndLoanTypeAndCreatedAtAfterOrderByCreatedAtDesc(
                            request.phone().trim(),
                            request.loanAmount(),
                            normalizedLoanType,
                            duplicateWindow
                    )
                    .map(LeadResponse::from)
                    .orElseGet(() -> saveLead(request, normalizedLoanType, null));
        }

        leadBackupService.markCommitted(opId);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> getLeads(Pageable pageable) {
        return leadRepository.findAll(pageable).map(LeadResponse::from);
    }

    private LeadResponse saveLead(LeadSubmitRequest request, String loanType, String idempotencyKey) {
        Integer extractedCibil = null;
        String metaString = null;

        // 🧠 DATA LOSS PREVENTION: Safely parse and serialize the incoming React metadata payload
        if (request.metadata() != null) {
            if (request.metadata().get("cibilScore") != null) {
                try {
                    extractedCibil = Integer.parseInt(request.metadata().get("cibilScore").toString());
                } catch (NumberFormatException ignored) {
                    // Failsafe in case frontend sends invalid number
                }
            }
            try {
                metaString = JSON_MAPPER.writeValueAsString(request.metadata());
            } catch (Exception e) {
                metaString = "{}";
            }
        }

        Lead lead = Lead.builder()
                .userName(request.userName().trim())
                .phone(request.phone().trim())
                .loanAmount(request.loanAmount())
                .loanType(loanType)
                .cibilScore(extractedCibil) // Injects the captured CIBIL directly into the column
                .metadata(metaString)       // Injects the JSON blob into the TEXT column
                .status(LeadStatus.NEW)
                .offerId(request.offerId())
                .idempotencyKey(idempotencyKey)
                .build();

        try {
            // 🧠 FIX: Use saveAndFlush to trigger DB-level unique constraints immediately
            // This prevents TOCTOU race conditions where two threads pass the application-level check
            return LeadResponse.from(leadRepository.saveAndFlush(lead));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException("Lead submission failed. A lead with this idempotency key already exists.");
        }
    }

    private String normalizeKey(String key) {
        return UUID.nameUUIDFromBytes(key.trim().toLowerCase().getBytes()).toString();
    }

    private String normalizeLoanType(String loanType) {
        String normalized = loanType == null ? "" : loanType.trim().toLowerCase();
        if (!ALLOWED_LOAN_TYPES.contains(normalized)) {
            throw new ConflictException("Unsupported loanType. Allowed: personal, business, home, education, lap");
        }
        return normalized;
    }
}
