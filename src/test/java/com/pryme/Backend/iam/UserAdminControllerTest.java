package com.pryme.Backend.iam;

import com.pryme.Backend.crm.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    private UserAdminController userAdminController;

    @BeforeEach
    void setUp() {
        userAdminController = new UserAdminController(
                userRepository,
                sessionManager,
                sessionRepository,
                loanApplicationRepository
        );
    }

    @Test
    void deleteUser_SuccessfullyDeletesUser() {
        UUID targetId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();

        User targetUser = new User();
        targetUser.setId(targetId);
        targetUser.setEmail("employee@pryme.com");
        targetUser.setRole(Role.EMPLOYEE);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(sessionRepository.findByUserId(targetId)).thenReturn(Collections.emptyList());

        var auth = new UsernamePasswordAuthenticationToken(
                callerId,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );

        ResponseEntity<?> response = userAdminController.deleteUser(targetId, auth);

        assertEquals(200, response.getStatusCode().value());
        verify(sessionManager).revokeAllUserSessions(targetId);
        verify(sessionRepository).deleteAll(Collections.emptyList());
        verify(loanApplicationRepository).clearAssigneeByAssigneeId(targetId);
        verify(userRepository).delete(targetUser);
    }

    @Test
    void deleteUser_RejectsSelfDeletion() {
        UUID targetId = UUID.randomUUID();

        User targetUser = new User();
        targetUser.setId(targetId);
        targetUser.setEmail("superadmin@pryme.com");
        targetUser.setRole(Role.SUPER_ADMIN);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));

        var auth = new UsernamePasswordAuthenticationToken(
                targetId,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );
        // Spring Security authentication getName returns principal or credentials, in our AuthController it returns getName as email.
        // Let's set the name on authentication to the email to match.
        UsernamePasswordAuthenticationToken authWithEmail = new UsernamePasswordAuthenticationToken(
                "superadmin@pryme.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );

        ResponseEntity<?> response = userAdminController.deleteUser(targetId, authWithEmail);

        assertEquals(400, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Cannot delete your own account.", body.get("message"));
        verifyNoInteractions(sessionManager, sessionRepository, loanApplicationRepository);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_RejectsAdminDeletingSuperAdmin() {
        UUID targetId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();

        User targetUser = new User();
        targetUser.setId(targetId);
        targetUser.setEmail("superadmin@pryme.com");
        targetUser.setRole(Role.SUPER_ADMIN);

        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));

        UsernamePasswordAuthenticationToken adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@pryme.com",
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        ResponseEntity<?> response = userAdminController.deleteUser(targetId, adminAuth);

        assertEquals(403, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Only a Super Admin can delete a Super Admin.", body.get("message"));
        verifyNoInteractions(sessionManager, sessionRepository, loanApplicationRepository);
        verify(userRepository, never()).delete(any());
    }
}
