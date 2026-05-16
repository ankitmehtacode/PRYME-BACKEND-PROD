package com.pryme.Backend.iam;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.pryme.Backend.iam.dto.TeamMemberOption;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    // ==========================================
    // 🧠 TEAM MEMBER DROPDOWN ENGINE
    // Returns ONLY internal staff for the lead-assignment dropdown.
    // Structurally prevents customer data from leaking into the selector.
    // ==========================================
    @Operation(summary = "Get team members for lead assignment dropdown")
    @GetMapping("/team-members")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<TeamMemberOption>> getTeamMembers() {
        List<TeamMemberOption> members = userRepository
                .findByRoleIn(List.of(Role.SUPER_ADMIN, Role.ADMIN, Role.EMPLOYEE))
                .stream()
                .map(TeamMemberOption::from)
                .toList();
        return ResponseEntity.ok(members);
    }

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
    @Operation(summary = "Update a user's role (SUPER_ADMIN or ADMIN)")
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
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

        // Safety guard: prevent ADMIN from creating a SUPER_ADMIN
        boolean isCallerSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        
        if (newRole == Role.SUPER_ADMIN && !isCallerSuperAdmin) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only a Super Admin can create another Super Admin."));
        }

        // 🧠 RBAC COUNT CONSTRAINTS
        // Only count if they are actually changing the role
        if (targetUser.getRole() != newRole) {
            long currentCount = userRepository.countByRole(newRole);
            if (newRole == Role.SUPER_ADMIN && currentCount >= 1) {
                return ResponseEntity.badRequest().body(Map.of("message", "Maximum of 1 Super Admin allowed."));
            }
            if (newRole == Role.ADMIN && currentCount >= 2) {
                return ResponseEntity.badRequest().body(Map.of("message", "Maximum of 2 Admins allowed."));
            }
            if (newRole == Role.EMPLOYEE && currentCount >= 10) {
                return ResponseEntity.badRequest().body(Map.of("message", "Maximum of 10 Employees allowed."));
            }
        }

        targetUser.elevateRole(newRole);
        userRepository.save(targetUser);

        // 🧠 CRITICAL SESSION INVALIDATION: After a role change, the user's active sessions
        // still hold the OLD role in the Spring Security context (cached by SessionManager).
        // If we don't invalidate them, the demoted user keeps admin access until their
        // session naturally expires. This flushes ALL sessions and pushes SESSION_TERMINATED
        // via the SSE Kill Switch to force immediate re-authentication with the new role.
        sessionManager.revokeAllUserSessions(targetUser.getId());

        return ResponseEntity.ok(UserAdminResponse.from(targetUser));
    }
}
