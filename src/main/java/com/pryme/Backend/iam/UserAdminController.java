package com.pryme.Backend.iam;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserRepository userRepository;

    @Operation(summary = "Get all users for Admin Dashboard")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        // Fetches all users and safely projects them to DTOs, stripping sensitive data.
        List<UserAdminResponse> responses = userRepository.findAll().stream()
                .map(UserAdminResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * 🧠 ROLE ELEVATION ENDPOINT — SUPER_ADMIN EXCLUSIVE
     *
     * Changes a team member's role. Safety guards:
     * 1. Only SUPER_ADMIN can invoke (Spring Security @PreAuthorize)
     * 2. Cannot demote yourself (prevents admin lockout)
     * 3. Cannot assign USER role via this endpoint (use registration flow)
     */
    @Operation(summary = "Update a user's role (SUPER_ADMIN only)")
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String newRoleStr = body.get("role");
        if (newRoleStr == null || newRoleStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required."));
        }

        Role newRole;
        try {
            newRole = Role.valueOf(newRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid role: " + newRoleStr));
        }

        // Safety guard: prevent assigning USER via admin panel (use registration flow)
        if (newRole == Role.USER) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot assign USER role via admin panel."));
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Safety guard: prevent self-demotion lockout
        String callerEmail = authentication.getName();
        if (targetUser.getEmail().equalsIgnoreCase(callerEmail)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot modify your own role."));
        }

        targetUser.elevateRole(newRole);
        userRepository.save(targetUser);

        return ResponseEntity.ok(UserAdminResponse.from(targetUser));
    }
}
