package com.pryme.Backend.iam;

import com.pryme.Backend.common.ForbiddenException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.pryme.Backend.common.UnauthorizedException;
import com.pryme.Backend.common.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.pryme.Backend.crm.LeadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    @Mock
    private SessionCookieHelper cookieHelper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private LeadRepository leadRepository;
    @Mock
    private UserIdGeneratorService userIdGeneratorService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        // 🧠 Core Engine Initialization (now includes SessionCookieHelper)
        authController = new AuthController(userRepository, passwordEncoder, sessionManager, cookieHelper, objectMapper, leadRepository, userIdGeneratorService);
    }

    // ==========================================
    // 🧠 SIGNUP ENGINE TESTS
    // ==========================================
    @Test
    void signup_SuccessfullyCreatesNewUser() {
        when(userRepository.findByEmail("newclient@pryme.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securePass123")).thenReturn("hashed_securePass123");

        SignupRequest request = new SignupRequest("New Client", "newclient@pryme.com", "securePass123", "9876543210");
        ResponseEntity<Map<String, String>> response = authController.register(request);

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

        SignupRequest request = new SignupRequest("Imposter", "existing@pryme.com", "password", null);

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> authController.register(request)
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

        HttpServletResponse mockResponse = org.mockito.Mockito.mock(HttpServletResponse.class);

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> authController.login(new LoginRequest("x@pryme.com", "bad", "web", null), mockResponse)
        );

        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void loginRejectsWrongPasswordWithUnauthorizedException() {
        User user = new User();
        user.setEmail("admin@pryme.com");
        user.setPasswordHash("hashed_password");
        
        when(userRepository.findByEmail("admin@pryme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        HttpServletResponse mockResponse = org.mockito.Mockito.mock(HttpServletResponse.class);

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> authController.login(new LoginRequest("admin@pryme.com", "wrong_password", "web", null), mockResponse)
        );

        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void loginSuccessfulLoginReturnsResponseAndSetsCookie() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("admin@pryme.com");
        user.setFullName("Admin User");
        user.setRole(Role.ADMIN);
        user.setPasswordHash("hashed_password");

        when(userRepository.findByEmail("admin@pryme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct_password", "hashed_password")).thenReturn(true);
        when(cookieHelper.getTtlSeconds()).thenReturn(3600L);
        
        UUID sessionId = UUID.randomUUID();
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setId(sessionId);
        sessionRecord.setExpiresAt(java.time.Instant.now().plusSeconds(3600));

        when(sessionManager.registerSession(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(user), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("web"), org.mockito.ArgumentMatchers.anyString())).thenReturn(sessionRecord);
        when(cookieHelper.createSessionCookie(sessionId.toString(), 3600L)).thenReturn(org.springframework.http.ResponseCookie.from("PRYME_SID", sessionId.toString()).build());

        HttpServletResponse mockResponse = org.mockito.Mockito.mock(HttpServletResponse.class);

        ResponseEntity<LoginResponse> response = authController.login(new LoginRequest("admin@pryme.com", "correct_password", "web", null), mockResponse);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id());
        assertEquals("admin@pryme.com", response.getBody().user().email());
        
        verify(mockResponse).addHeader(org.mockito.ArgumentMatchers.eq(org.springframework.http.HttpHeaders.SET_COOKIE), org.mockito.ArgumentMatchers.anyString());
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
