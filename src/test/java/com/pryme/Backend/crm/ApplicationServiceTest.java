package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repository;

    @InjectMocks
    private ApplicationService service;

    @Test
    void assign_throwsNotFound_whenMissingApplication() {
        when(repository.findByApplicationId("PRY-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign("PRY-1", new AssignLeadRequest("EMP001", null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatus_throwsConflict_forInvalidStatus() {
        LoanApplication app = LoanApplication.builder()
                .id(UUID.randomUUID())
                .applicationId("PRY-2")
                .assignee("UNASSIGNED")
                .version(1L)
                .build();
        when(repository.findByApplicationId("PRY-2")).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> service.updateStatus("PRY-2", new UpdateStatusRequest("WRONG", 1L)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void assign_setsNormalizedAssignee() {
        LoanApplication app = LoanApplication.builder()
                .id(UUID.randomUUID())
                .applicationId("PRY-3")
                .assignee("UNASSIGNED")
                .status(ApplicationStatus.SUBMITTED)
                .version(2L)
                .build();

        when(repository.findByApplicationId("PRY-3")).thenReturn(Optional.of(app));
        when(repository.save(app)).thenReturn(app);

        ApplicationResponse response = service.assign("PRY-3", new AssignLeadRequest("emp001", 2L));

        assertThat(response.assignee()).isEqualTo("EMP001");
    }
}
