package com.pryme.Backend.document;

import com.pryme.Backend.common.ForbiddenException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1") // 🧠 Top-level routing to support both RESTful and RPC paths
@RequiredArgsConstructor
public class DocumentVaultController {

    private static final Logger log = LoggerFactory.getLogger(DocumentVaultController.class);
    private final DocumentVaultService vaultService;
    private final S3PresignedUrlService s3PresignedUrlService;

    // ==========================================
    // 🧠 FAILPROOF PRINCIPAL EXTRACTOR
    // ==========================================
    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ForbiddenException("Security matrix violation: Authentication required.");
        }
        try {
            return auth.getPrincipal() instanceof UUID ?
                    (UUID) auth.getPrincipal() :
                    UUID.fromString(auth.getPrincipal().toString());
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("Invalid security token footprint.");
        }
    }

    // ==========================================
    // 🧠 1. IDENTITY VERIFICATION GATEWAY
    // ==========================================
    @PostMapping("/documents/verify-id")
    public ResponseEntity<Map<String, String>> verifyIdentity(@Valid @RequestBody VerifyIdRequest request) {
        log.info("Vault Gateway: Verifying Identity Matrix for Application {}", request.applicationId());

        boolean verified = vaultService.verifyIdentityMatrix(request.applicationId(), request.idType(), request.idNumber());

        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAILED",
                    "message", "Identity verification failed. Invalid signature."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "VERIFIED",
                "message", "Identity signature securely validated."
        ));
    }

    @PostMapping("/documents/initiate-upload")
    public ResponseEntity<S3PresignedUrlService.PresignedUrlResponse> initiateUpload(@Valid @RequestBody DocumentUploadRequest request) {
        vaultService.markAwaitingUpload(request.documentId());
        return ResponseEntity.ok(s3PresignedUrlService.generateUploadUrl(request.documentId(), request.contentType()));
    }

    // ==========================================
    // 🧠 3. SECURE METADATA RETRIEVAL ENGINE
    // ==========================================
    @GetMapping({"/applications/{applicationId}/documents", "/documents/{applicationId}"})
    public ResponseEntity<List<DocumentMetadataResponse>> applicationDocuments(@PathVariable String applicationId) {
        log.info("Vault Gateway: Retrieving document metadata matrix for Application {}", applicationId);
        return ResponseEntity.ok(vaultService.getApplicationDocuments(applicationId));
    }

    // ==========================================
    // 🧠 4. ZERO-TRUST BINARY STREAMING GATEWAY (NEW)
    // ==========================================
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {

        // 1. Mandatory Security Check
        extractUserId(authentication);

        log.info("Vault Gateway: Requesting secure binary stream for Document {}", documentId);

        // 2. Fetch the metadata to correctly set HTTP Headers
        DocumentRecord metadata = vaultService.getDocumentMetadata(documentId);

        // 3. Initiate the Byte-Stream
        Resource resource = vaultService.loadDocumentAsResource(documentId);

        // 4. Pipe directly to HTTP Response
        // Using "inline" allows PDFs/Images to open directly in the browser instead of forcing a download
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }
}
