package com.pryme.Backend.crm;

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

    private static final Set<String> ALLOWED_LOAN_TYPES = Set.of("personal", "business", "home", "education");

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
            LocalDateTime duplicateWindow = LocalDateTime.now().minusMinutes(2);
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
        Lead lead = Lead.builder()
                .userName(request.userName().trim())
                .phone(request.phone().trim())
                .loanAmount(request.loanAmount())
                .loanType(loanType)
                .status(LeadStatus.NEW)
                .offerId(request.offerId())
                .idempotencyKey(idempotencyKey)
                .build();

        return LeadResponse.from(leadRepository.save(lead));
    }

    private String normalizeKey(String key) {
        return UUID.nameUUIDFromBytes(key.trim().toLowerCase().getBytes()).toString();
    }

    private String normalizeLoanType(String loanType) {
        String normalized = loanType == null ? "" : loanType.trim().toLowerCase();
        if (!ALLOWED_LOAN_TYPES.contains(normalized)) {
            throw new ConflictException("Unsupported loanType. Allowed: personal, business, home, education");
        }
        return normalized;
    }
}
