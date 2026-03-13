package com.pryme.Backend.crm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadBackupService leadBackupService;

    @InjectMocks
    private LeadService leadService;

    @Test
    void submitLead_returnsExistingLeadForSameIdempotencyKey() {
        UUID opId = UUID.randomUUID();
        when(leadBackupService.begin(any(), any())).thenReturn(opId);

        // 🧠 MOCK STATE: Added cibilScore to accurately reflect the new Entity schema
        Lead existing = Lead.builder()
                .id(UUID.randomUUID())
                .userName("Rahul")
                .phone("9876543210")
                .loanAmount(new BigDecimal("500000.00"))
                .loanType("personal")
                .cibilScore(780)
                .status(LeadStatus.NEW)
                .offerId("axis-pre")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        when(leadRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existing));

        // 🧠 PRODUCTION FIX: Injecting the 6th parameter (Metadata Map) to perfectly simulate the React Frontend payload
        LeadResponse response = leadService.submitLead(
                new LeadSubmitRequest(
                        "Rahul",
                        "9876543210",
                        new BigDecimal("500000.00"),
                        "personal",
                        "axis-pre",
                        Map.of("cibilScore", 780, "email", "rahul@pryme.in") // Simulated frontend JSON blob
                ),
                "my-request"
        );

        assertThat(response.userName()).isEqualTo("Rahul");
        verify(leadRepository, never()).save(any());
        verify(leadBackupService).markCommitted(opId);
    }

    @Test
    void submitLead_persistsNewLeadWithoutIdempotencyKey() {
        UUID opId = UUID.randomUUID();
        when(leadBackupService.begin(any(), any())).thenReturn(opId);

        Lead saved = Lead.builder()
                .id(UUID.randomUUID())
                .userName("Asha")
                .phone("9123456789")
                .loanAmount(new BigDecimal("900000.00"))
                .loanType("business")
                .status(LeadStatus.NEW)
                .offerId("icici-cashback")
                .build();

        when(leadRepository.findTopByPhoneAndLoanAmountAndLoanTypeAndCreatedAtAfterOrderByCreatedAtDesc(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(leadRepository.save(any(Lead.class))).thenReturn(saved);

        // 🧠 PRODUCTION FIX: Passing an empty Map.of() to fulfill constructor requirements for leads missing metadata
        LeadResponse response = leadService.submitLead(
                new LeadSubmitRequest(
                        "Asha",
                        "9123456789",
                        new BigDecimal("900000.00"),
                        "business",
                        "icici-cashback",
                        Map.of() // Prevents Java 'actual and formal argument lists differ in length' compilation error
                ),
                null
        );

        assertThat(response.offerId()).isEqualTo("icici-cashback");
        verify(leadRepository, times(1)).save(any(Lead.class));
        verify(leadBackupService).markCommitted(opId);
    }
}