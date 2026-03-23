package com.pryme.Backend.crm;

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    // 🧠 PRODUCTION FIX: Mock both required repositories
    @Mock
    private LoanApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationStatusHistoryRepository historyRepository;

    @InjectMocks
    private ApplicationService service;

    @BeforeEach
    void setup() {
        service = new ApplicationService(applicationRepository, userRepository, historyRepository);
    }

    @Test
    void assign_throwsNotFound_whenMissingApplication() {
        when(applicationRepository.findByApplicationId("PRY-1")).thenReturn(Optional.empty());

        // 🧠 Valid UUID format used instead of "EMP001" to pass the updated strict validation
        assertThatThrownBy(() -> service.assign("PRY-1", new AssignLeadRequest(UUID.randomUUID().toString(), null), UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatus_throwsConflict_forInvalidStatus() {
        LoanApplication app = LoanApplication.builder()
                .id(UUID.randomUUID())
                .applicationId("PRY-2")
                // 🧠 PRODUCTION FIX: Removed the illegal String injection.
                // Unassigned relationships naturally default to null in Relational Databases.
                .version(1L)
                .build();

        when(applicationRepository.findByApplicationId("PRY-2")).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> service.updateStatus("PRY-2", new UpdateStatusRequest("WRONG", 1L), UUID.randomUUID()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void assign_setsUserAssignee() {
        UUID employeeId = UUID.randomUUID();
        LoanApplication app = LoanApplication.builder()
                .id(UUID.randomUUID())
                .applicationId("PRY-3")
                .status(ApplicationStatus.SUBMITTED)
                .version(2L)
                .build();

        // 🧠 PRODUCTION FIX: Mocking the actual User entity to satisfy the Foreign Key constraint
        User mockEmployee = mock(User.class);
        when(mockEmployee.getFullName()).thenReturn("John Doe");

        when(applicationRepository.findByApplicationId("PRY-3")).thenReturn(Optional.of(app));
        when(userRepository.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        when(applicationRepository.save(app)).thenReturn(app);

        ApplicationResponse response = service.assign("PRY-3", new AssignLeadRequest(employeeId.toString(), 2L), UUID.randomUUID());

        // Ensures the DTO safely unpacked the User entity's string name
        assertThat(response.assignee()).isEqualTo("John Doe");
    }
}
