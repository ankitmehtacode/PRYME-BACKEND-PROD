package com.pryme.Backend.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryme.Backend.common.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.pryme.Backend.iam.UserRepository;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.Role;
import com.pryme.Backend.common.NotFoundException;

@Service
@RequiredArgsConstructor
public class LeadService {

    // 🧠 PRODUCTION FIX: Aligned with frontend product catalog (accepts both short and enum formats)
    private static final Set<String> ALLOWED_LOAN_TYPES = Set.of(
            "personal", "business", "home", "education", "lap", "auto"
    );

    // 🧠 PRODUCTION FIX: Static, thread-safe mapper prevents memory leaks during high-volume JSON serialization
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final LeadRepository leadRepository;
    private final LeadBackupService leadBackupService;
    private final UserRepository userRepository;

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

    /**
     * 🧠 RBAC-SCOPED LEAD RETRIEVAL WITH BATCH NAME RESOLUTION:
     * - EMPLOYEE: sees only leads assigned to them
     * - ADMIN/SUPER_ADMIN: sees the full pipeline
     * 
     * Assignee names are resolved in ONE batch query (N+1 prevention).
     */
    @Transactional(readOnly = true)
    public Page<LeadResponse> getLeads(Pageable pageable) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        Page<Lead> leadPage;
        if (auth != null && auth.isAuthenticated()) {
            boolean isEmployee = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (isEmployee && !isAdmin) {
                User caller = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new NotFoundException("Caller not found"));
                leadPage = leadRepository.findByAssignedTo(caller.getId(), pageable);
            } else {
                leadPage = leadRepository.findAll(pageable);
            }
        } else {
            leadPage = leadRepository.findAll(pageable);
        }

        // 🧠 BATCH NAME RESOLUTION: One query to resolve all assignee names instead of N+1
        Map<UUID, String> assigneeNameMap = resolveAssigneeNames(leadPage.getContent());

        return leadPage.map(lead -> LeadResponse.from(lead, assigneeNameMap.get(lead.getAssignedTo())));
    }

    @Transactional
    public LeadResponse assignLead(UUID leadId, UUID assigneeId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead not found with ID: " + leadId));

        String assigneeName = null;
        if (assigneeId == null) {
            lead.setAssignedTo(null);
        } else {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found with ID: " + assigneeId));

            if (assignee.getRole() == Role.USER) {
                throw new ConflictException("Cannot assign leads to standard users. Must be a team member.");
            }
            lead.setAssignedTo(assigneeId);
            assigneeName = assignee.getFullName();
        }

        return LeadResponse.from(leadRepository.save(lead), assigneeName);
    }

    /**
     * 🧠 N+1 PREVENTION ENGINE: Collects all unique assignee UUIDs from a page of leads,
     * fetches them in a single SELECT ... WHERE id IN (...) query, and builds a lookup map.
     */
    private Map<UUID, String> resolveAssigneeNames(List<Lead> leads) {
        Set<UUID> assigneeIds = leads.stream()
                .map(Lead::getAssignedTo)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (assigneeIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(assigneeIds).stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));
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
        // 🧠 PIPELINE FIX: Frontend sends HOME_LOAN, PERSONAL_LOAN, etc.
        // Strip _loan suffix for backward compat: home_loan → home
        if (normalized.endsWith("_loan")) {
            normalized = normalized.replace("_loan", "");
        }
        if (!ALLOWED_LOAN_TYPES.contains(normalized)) {
            throw new ConflictException("Unsupported loanType '" + loanType + "'. Allowed: personal, business, home, education, lap, auto");
        }
        return normalized;
    }
}
