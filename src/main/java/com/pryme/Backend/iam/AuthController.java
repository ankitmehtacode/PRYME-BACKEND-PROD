package com.pryme.Backend.iam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password().trim(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        SessionRecord session = sessionManager.issueSession(user.getId(), request.deviceId());

        return ResponseEntity.ok(new LoginResponse(
                session.token(),
                user.getRole().name(),
                user.getFullName(),
                session.expiresAt(),
                "Login successful"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        sessionManager.invalidate(token);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<SessionRecord>> sessions(@PathVariable UUID userId) {
        return ResponseEntity.ok(sessionManager.activeSessions(userId));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Bearer token");
        }
        return authHeader.substring(7);
    }
}

record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        String deviceId
) {
}

record LoginResponse(
        String token,
        String role,
        String name,
        Instant expiresAt,
        String message
) {
}
