package com.pryme.Backend.iam;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.UnauthorizedException;
import com.pryme.Backend.common.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

package com.pryme.Backend.iam;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.UnauthorizedException;
import com.pryme.Backend.common.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
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
        // 🧠 Core Engine Initialization
        authController = new AuthController(userRepository, passwordEncoder, sessionManager);
    }

    // ==========================================
    // 🧠 SIGNUP ENGINE TESTS
    // ==========================================
    @Test
    void signup_SuccessfullyCreatesNewUser() {
        when(userRepository.findByEmail("newclient@pryme.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securePass123")).thenReturn("hashed_securePass123");

        SignupRequest request = new SignupRequest("New Client", "newclient@pryme.com", "securePass123");
        ResponseEntity<Map<String, String>> response = authController.signup(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("newclient@pryme.com", response.getBody().get("email"));

        // Verify the database persistence layer receives the correct encrypted payload
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("New Client", savedUser.getFullName());
        assertEquals("hashed_securePass123", savedUser.getPasswordHash());

        // 🧠 STRICT SECURITY ASSERTION: Assures no privilege escalation vulnerabilities
        assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    void signup_RejectsDuplicateEmailWithConflictException() {
        // Simulate a collision with an existing user in the database
        User existingUser = new User();
        existingUser.setEmail("existing@pryme.com");

        when(userRepository.findByEmail("existing@pryme.com")).thenReturn(Optional.of(existingUser));

        SignupRequest request = new SignupRequest("Imposter", "existing@pryme.com", "password");

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> authController.signup(request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    // ==========================================
    // EXISTING IDENTITY TESTS
    // ==========================================
    @Test
    void meReturnsCurrentUserProfile() {
        UUID userId = UUID.randomUUID();

        // 🧠 PRODUCTION FIX: Bypassing Lombok Builder to mathematically guarantee CI/CD compilation
        User user = new User();
        user.setId(userId);
        user.setEmail("admin@pryme.com");
        user.setFullName("Admin");
        user.setPhone("9999999999");
        user.setRole(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var auth = new UsernamePasswordAuthenticationToken(
                userId,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        ResponseEntity<MeResponse> response = authController.me(auth);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        // Assumes MeResponse is a modern Java Record
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

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> authController.sessions(otherUserId, auth)
        );

        assertEquals("Unauthorized to view sessions", ex.getMessage());
    }
}