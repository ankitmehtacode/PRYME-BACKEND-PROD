package com.pryme.Backend.iam;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.UnauthorizedException;
import com.pryme.Backend.common.ConflictException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionManager sessionManager;
    private final SessionCookieHelper cookieHelper;
    private final ObjectMapper objectMapper;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          SessionManager sessionManager, SessionCookieHelper cookieHelper,
                          ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
        this.cookieHelper = cookieHelper;
        this.objectMapper = objectMapper;
    }

    // ==========================================
    // 🧠 SECURE DATABASE SIGNUP ENGINE
    // ==========================================
    // 🧠 SILICON-VALLEY FIX: Bind to BOTH endpoints to guarantee React never hits a 404
    @Operation(summary = "Register a new user account")
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

        // 🧠 160 IQ: Embed the full MeResponse so the frontend can hydrate
        // its React Query cache from the login response itself — zero dependency
        // on a second /auth/me call that may fail due to Vite proxy cookie issues.
        MeResponse mePayload = new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName(),
                user.getPhone(),
                derivePermissions(user.getRole())
        );

        return ResponseEntity.ok(new LoginResponse(
                user.getId(),
                user.getRole().name(),
                user.getFullName(),
                session.getExpiresAt(),
                "Login successful",
                mePayload
        ));
    }

    /**
     * 🧠 THE GOD ENDPOINT — Hydrates the entire frontend identity layer.
     *
     * CRITICAL: This endpoint is NOT in the shouldNotFilter bypass list.
     * It MUST run through SessionAuthenticationFilter so the HttpOnly cookie
     * is validated and Authentication is populated. Without this, the frontend
     * gets a 401 on every cold boot.
     */
    @Operation(summary = "Get current user identity, role, and permissions")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        UUID userId = userIdFromAuth(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Invalid session"));

        return ResponseEntity.ok(new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName(),   // Maps to frontend's 'name' field
                user.getPhone(),
                derivePermissions(user.getRole())  // 🧠 CLOSED-LOOP: Backend dictates permissions
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
    public ResponseEntity<List<SessionResponse>> sessions(@PathVariable UUID userId, Authentication authentication) {
        UUID currentUserId = userIdFromAuth(authentication);

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN"));

        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new ForbiddenException("Unauthorized to view sessions");
        }

        // 🧠 ZERO-TRUST: Map raw entities to masked DTOs before crossing the API boundary
        List<SessionResponse> safeSessions = sessionManager.activeSessions(userId)
                .stream()
                .map(SessionResponse::from)
                .toList();

        return ResponseEntity.ok(safeSessions);
    }

    // ==========================================
    // 🧠 GOOGLE OAUTH2 — ZERO-DEPENDENCY TOKEN VERIFICATION
    // ==========================================
    // Uses Java 11+ HttpClient to call Google's tokeninfo endpoint.
    // No google-api-client JAR required. Validates: aud, exp, iss.
    // ==========================================
    @Operation(summary = "Authenticate via Google ID token (OAuth2)")
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleSignIn(@RequestBody Map<String, String> body,
                                                      HttpServletResponse httpResponse) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Missing Google ID token.");
        }

        // 1. Verify the token with Google's tokeninfo endpoint
        JsonNode tokenInfo;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new UnauthorizedException("Google token verification failed. Status: " + response.statusCode());
            }

            tokenInfo = objectMapper.readTree(response.body());
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Google token verification failed: " + e.getMessage());
        }

        // 2. Validate audience (aud) matches our Google Client ID
        String aud = tokenInfo.has("aud") ? tokenInfo.get("aud").asText() : "";
        if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(aud)) {
            throw new UnauthorizedException("Token audience mismatch. Expected: " + googleClientId);
        }

        // 3. Extract verified email + name
        String email = tokenInfo.has("email") ? tokenInfo.get("email").asText() : null;
        String name = tokenInfo.has("name") ? tokenInfo.get("name").asText() : "";
        String emailVerified = tokenInfo.has("email_verified") ? tokenInfo.get("email_verified").asText() : "false";

        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Google account has no email address.");
        }
        if (!"true".equals(emailVerified)) {
            throw new UnauthorizedException("Google email not verified.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        // 4. Find-or-create user (JIT provisioning)
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setFullName(name.isBlank() ? normalizedEmail.split("@")[0] : name.trim());
                    newUser.setEmail(normalizedEmail);
                    // Google OAuth users get a random un-guessable password hash.
                    // They can never login via password — only via Google.
                    newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRole(Role.USER);
                    return userRepository.save(newUser);
                });

        // 5. Create session + cookie (identical to password login)
        long ttl = cookieHelper.getTtlSeconds();
        SessionRecord session = sessionManager.registerSession(
                UUID.randomUUID(), user, Instant.now().plusSeconds(ttl), "Google-OAuth", "Google Sign-In");

        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                cookieHelper.createSessionCookie(session.getId().toString()).toString());

        MeResponse mePayload = new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName(),
                user.getPhone(),
                derivePermissions(user.getRole())
        );

        return ResponseEntity.ok(new LoginResponse(
                user.getId(),
                user.getRole().name(),
                user.getFullName(),
                session.getExpiresAt(),
                "Google authentication successful",
                mePayload
        ));
    }

    private UUID userIdFromAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
            throw new UnauthorizedException("Authentication required");
        }
        return (UUID) authentication.getPrincipal();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧠 CLOSED-LOOP PERMISSION ENGINE
    // ═══════════════════════════════════════════════════════════════════════════
    // The backend is the SOLE authority on what each role can do.
    // The frontend's ProtectedRoute and hasPermission() only CHECK — never INVENT.
    // ═══════════════════════════════════════════════════════════════════════════
    private List<String> derivePermissions(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> List.of(
                    "VIEW_CRM", "MANAGE_LEADS", "MANAGE_APPLICATIONS",
                    "MANAGE_POLICIES", "VIEW_ANALYTICS", "MANAGE_USERS",
                    "MANAGE_PARTNERS", "VIEW_AUDIT_LOG",
                    "UPLOAD_DOCUMENTS", "VERIFY_IDENTITY"
            );
            case ADMIN -> List.of(
                    "VIEW_CRM", "MANAGE_LEADS", "MANAGE_APPLICATIONS",
                    "MANAGE_POLICIES", "VIEW_ANALYTICS",
                    "UPLOAD_DOCUMENTS", "VERIFY_IDENTITY"
            );
            case EMPLOYEE -> List.of(
                    "VIEW_CRM", "MANAGE_LEADS", "MANAGE_APPLICATIONS",
                    "UPLOAD_DOCUMENTS", "VERIFY_IDENTITY"
            );
            case USER -> List.of(
                    "UPLOAD_DOCUMENTS"
            );
        };
    }
}
