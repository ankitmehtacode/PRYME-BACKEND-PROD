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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.iam.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadBackupService leadBackupService;

    @Mock
    private UserRepository userRepository;

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
        when(leadRepository.saveAndFlush(any(Lead.class))).thenReturn(saved);

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
        verify(leadRepository, times(1)).saveAndFlush(any(Lead.class));
        verify(leadBackupService).markCommitted(opId);
    }

    @Test
    void updateLeadStatus_adminCanUpdateLeadStatus() {
        // Setup security context as ADMIN
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        doReturn(true).when(authentication).isAuthenticated();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(leadId)
                .userName("Rahul")
                .phone("9876543210")
                .loanAmount(new BigDecimal("500000.00"))
                .loanType("personal")
                .status(LeadStatus.NEW)
                .build();

        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try {
            LeadResponse response = leadService.updateLeadStatus(leadId, LeadStatus.CONTACTED);
            assertThat(response.status()).isEqualTo("CONTACTED");
            verify(leadRepository).save(lead);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updateLeadStatus_employeeCanUpdateAssignedLead() {
        UUID callerId = UUID.randomUUID();
        // Setup security context as EMPLOYEE
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))).when(authentication).getAuthorities();
        doReturn(true).when(authentication).isAuthenticated();
        doReturn(callerId).when(authentication).getPrincipal();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(leadId)
                .userName("Rahul")
                .phone("9876543210")
                .loanAmount(new BigDecimal("500000.00"))
                .loanType("personal")
                .status(LeadStatus.NEW)
                .assignedTo(callerId)
                .build();

        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        com.pryme.Backend.iam.User caller = com.pryme.Backend.iam.User.builder()
                .id(callerId)
                .fullName("John Employee")
                .build();
        when(userRepository.findById(callerId)).thenReturn(Optional.of(caller));

        try {
            LeadResponse response = leadService.updateLeadStatus(leadId, LeadStatus.CONTACTED);
            assertThat(response.status()).isEqualTo("CONTACTED");
            assertThat(response.assigneeName()).isEqualTo("John Employee");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updateLeadStatus_employeeCannotUpdateOtherLead() {
        UUID callerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        // Setup security context as EMPLOYEE
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))).when(authentication).getAuthorities();
        doReturn(true).when(authentication).isAuthenticated();
        doReturn(callerId).when(authentication).getPrincipal();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(leadId)
                .userName("Rahul")
                .phone("9876543210")
                .loanAmount(new BigDecimal("500000.00"))
                .loanType("personal")
                .status(LeadStatus.NEW)
                .assignedTo(otherId)
                .build();

        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));

        try {
            assertThatThrownBy(() -> leadService.updateLeadStatus(leadId, LeadStatus.CONTACTED))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Cannot update leads assigned to other team members");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}