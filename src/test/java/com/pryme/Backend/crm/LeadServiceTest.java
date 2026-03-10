package com.pryme.Backend.crm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private LeadService leadService;

    @Test
    void submitLead_returnsExistingLeadForSameIdempotencyKey() {
        Lead existing = Lead.builder()
                .id(UUID.randomUUID())
                .userName("Rahul")
                .phone("9876543210")
                .loanAmount(new BigDecimal("500000.00"))
                .loanType("personal")
                .status(LeadStatus.NEW)
                .offerId("axis-pre")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        when(leadRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existing));

        LeadResponse response = leadService.submitLead(
                new LeadSubmitRequest("Rahul", "9876543210", new BigDecimal("500000.00"), "personal", "axis-pre"),
                "my-request"
        );

        assertThat(response.userName()).isEqualTo("Rahul");
        verify(leadRepository, never()).save(any());
    }

    @Test
    void submitLead_persistsNewLeadWithoutIdempotencyKey() {
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

        LeadResponse response = leadService.submitLead(
                new LeadSubmitRequest("Asha", "9123456789", new BigDecimal("900000.00"), "business", "icici-cashback"),
                null
        );

        assertThat(response.offerId()).isEqualTo("icici-cashback");
        verify(leadRepository, times(1)).save(any(Lead.class));
    }
}
