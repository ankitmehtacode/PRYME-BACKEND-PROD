package com.pryme.Backend.iam;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.UnauthorizedException;
import com.pryme.Backend.common.ConflictException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionManager sessionManager;
    private final SessionCookieHelper cookieHelper;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          SessionManager sessionManager, SessionCookieHelper cookieHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
        this.cookieHelper = cookieHelper;
    }

    // ==========================================
    // 🧠 SECURE DATABASE SIGNUP ENGINE
    // ==========================================
    // 🧠 SILICON-VALLEY FIX: Bind to BOTH endpoints to guarantee React never hits a 404
    @Operation(summary = "One-line description of this endpoint")
    @PostMapping({"/register", "/signup"})
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody SignupRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        // 1. Prevent duplicate identities in the database
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("An account with this email already exists.");
        }

        // 2. Hash the password & Create the User Entity
        // We use standard instantiation to mathematically guarantee no Lombok compiler issues in Maven
        User newUser = new User();
        newUser.setFullName(request.fullName().trim());
        newUser.setEmail(normalizedEmail);
        newUser.setPasswordHash(passwordEncoder.encode(request.password().trim())); // Zero-Trust: Never store plaintext
        newUser.setRole(Role.USER); // Restrict default registration to standard Client Portal tier

        // 3. Persist physically to the database
        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of(
                "message", "Identity successfully provisioned in the database.",
                "email", newUser.getEmail()
        ));
    }

    // ==========================================
    // EXISTING IDENTITY ENGINES
    // ==========================================
    @Operation(summary = "Authenticate user and issue HttpOnly session cookie")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletResponse httpResponse) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password().trim(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // 🧠 Configurable TTL — reads app.session.ttl-seconds from YAML (default: 3600)
        long ttl = cookieHelper.getTtlSeconds();
        SessionRecord session = sessionManager.registerSession(
                UUID.randomUUID(), user, Instant.now().plusSeconds(ttl), request.deviceId(), "Unknown");

        // 🧠 SECURITY FIX: Session ID transported EXCLUSIVELY via HttpOnly cookie.
        // The browser cookie jar stores it. JavaScript physically CANNOT read it.
        // XSS payloads calling document.cookie or localStorage get nothing.
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                cookieHelper.createSessionCookie(session.getId().toString()).toString());

        return ResponseEntity.ok(new LoginResponse(
                user.getId(),
                user.getRole().name(),
                user.getFullName(),
                session.getExpiresAt(),
                "Login successful"
        ));
    }

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        UUID userId = userIdFromAuth(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Invalid session"));

        return ResponseEntity.ok(new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName(),
                user.getPhone()
        ));
    }

    @Operation(summary = "Terminate session and purge HttpOnly cookie")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest httpRequest,
                                                       HttpServletResponse httpResponse,
                                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 🧠 DUAL-READ: Cookie first (browser), Bearer fallback (mobile/API)
        String sessionId = cookieHelper.extractSessionId(httpRequest);
        if (sessionId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            sessionId = authHeader.substring(7);
        }
        if (sessionId == null) {
            throw new UnauthorizedException("No active session found.");
        }

        sessionManager.invalidate(UUID.fromString(sessionId));

        // 🧠 KILL THE COOKIE: maxAge=0 instructs the browser to purge it immediately
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookieHelper.createClearCookie().toString());

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @Operation(summary = "List active sessions for a user")
    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<SessionRecord>> sessions(@PathVariable UUID userId, Authentication authentication) {
        UUID currentUserId = userIdFromAuth(authentication);

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN"));

        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new ForbiddenException("Unauthorized to view sessions");
        }

        return ResponseEntity.ok(sessionManager.activeSessions(userId));
    }

    private UUID userIdFromAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
            throw new UnauthorizedException("Authentication required");
        }
        return (UUID) authentication.getPrincipal();
    }
}
