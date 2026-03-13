package com.pryme.Backend.iam;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.UnauthorizedException;
import com.pryme.Backend.common.ConflictException; // 🧠 STRICT VALIDATION
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
    }

    // ==========================================
    // 🧠 SECURE DATABASE SIGNUP ENGINE
    // ==========================================
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
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
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password().trim(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        SessionRecord session = sessionManager.issueSession(user.getId(), request.deviceId());

        return ResponseEntity.ok(new LoginResponse(
                user.getId(), // 🧠 CRITICAL: Passes the User ID to React for the Lead Elevation Engine
                session.token(),
                user.getRole().name(),
                user.getFullName(),
                session.expiresAt(),
                "Login successful"
        ));
    }

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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        sessionManager.invalidate(token);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

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

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing Bearer token");
        }
        return authHeader.substring(7);
    }
}