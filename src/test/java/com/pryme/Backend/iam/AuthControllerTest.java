package com.pryme.Backend.iam;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SessionManager sessionManager;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userRepository, passwordEncoder, sessionManager);
    }

    @Test
    void meReturnsCurrentUserProfile() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("admin@pryme.com")
                .fullName("Admin")
                .phone("9999999999")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var auth = new UsernamePasswordAuthenticationToken(
                userId,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        ResponseEntity<MeResponse> response = authController.me(auth);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id());
        assertEquals("admin@pryme.com", response.getBody().email());
        assertEquals("ADMIN", response.getBody().role());
    }

    @Test
    void loginRejectsUnknownEmailWithUnauthorizedException() {
        when(userRepository.findByEmail("x@pryme.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> authController.login(new LoginRequest("x@pryme.com", "bad", "web"))
        );

        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void sessionsRejectsDifferentUserForNonAdmin() {
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        var auth = new UsernamePasswordAuthenticationToken(
                currentUserId,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authController.sessions(otherUserId, auth));
        assertEquals("Unauthorized to view sessions", ex.getMessage());
    }
}
