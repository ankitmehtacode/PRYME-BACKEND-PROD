package com.pryme.Backend.iam;

import com.pryme.Backend.common.UnauthorizedException;
import com.pryme.Backend.document.S3PresignedUrlService;
import com.pryme.Backend.iam.dto.ProfileUpdateRequest;
import com.pryme.Backend.iam.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final S3PresignedUrlService s3PresignedUrlService;

    private UUID userIdFromAuth(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return auth.getPrincipal() instanceof UUID
                ? (UUID) auth.getPrincipal()
                : UUID.fromString(auth.getPrincipal().toString());
    }

    @Operation(summary = "Get full profile of the authenticated user")
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        UUID userId = userIdFromAuth(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @Operation(summary = "Update user profile and metadata")
    @PutMapping
    @Transactional
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        UUID userId = userIdFromAuth(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (request.fullName() != null && !request.fullName().isBlank()) user.setFullName(request.fullName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.city() != null) user.setCity(request.city());
        if (request.state() != null) user.setState(request.state());
        if (request.profilePictureUrl() != null) user.setProfilePictureUrl(request.profilePictureUrl());

        if (request.metadata() != null) {
            Map<String, Object> existingMetadata = user.getMetadata() != null ? new HashMap<>(user.getMetadata()) : new HashMap<>();
            existingMetadata.putAll(request.metadata());
            user.setMetadata(existingMetadata);
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @Operation(summary = "Generate S3 upload URL for profile picture")
    @PostMapping("/avatar/initiate-upload")
    public ResponseEntity<S3PresignedUrlService.PresignedUrlResponse> initiateAvatarUpload(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        UUID userId = userIdFromAuth(authentication);
        String contentType = request.getOrDefault("contentType", "image/jpeg");
        
        // Generate a unique object key for the user's avatar
        String documentId = "avatars/" + userId.toString() + "/" + UUID.randomUUID().toString();
        if (contentType.equals("image/png")) {
            documentId += ".png";
        } else {
            documentId += ".jpg";
        }

        return ResponseEntity.ok(s3PresignedUrlService.generateUploadUrl(documentId, contentType));
    }
}
