package com.pryme.Backend.document;

import io.swagger.v3.oas.annotations.Operation;

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
    private final S3PostPolicyService s3PostPolicyService;

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
    @Operation(summary = "One-line description of this endpoint")
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

    @Operation(summary = "Initiates a zero-trust S3 upload session")
    @PostMapping("/documents/initiate-upload")
    public ResponseEntity<S3PresignedUrlService.PresignedUrlResponse> initiateUpload(@Valid @RequestBody DocumentUploadRequest request) {
        DocumentRecord doc = vaultService.initiateDocumentUpload(request);
        return ResponseEntity.ok(s3PresignedUrlService.generateUploadUrl(doc.getS3ObjectKey(), request.contentType()));
    }

    /**
     * 🧠 EDGE-ENFORCED UPLOAD: Returns an S3 POST policy instead of a PUT presigned URL.
     * 
     * WHY: A PUT URL cannot enforce file size server-side. A POST policy cryptographically
     * bakes content-length-range into the AWS signature — if the upload exceeds 5MB,
     * AWS S3 rejects it at the edge. Our backend never sees the traffic.
     *
     * Frontend must build a FormData with all returned fields, append file LAST, then
     * POST to the returned endpoint.
     */
    @Operation(summary = "Initiates an edge-enforced S3 upload with POST policy (5MB max, type-locked)")
    @PostMapping("/documents/initiate-secure-upload")
    public ResponseEntity<S3PostPolicyService.PostPolicyResponse> initiateSecureUpload(
            @Valid @RequestBody DocumentUploadRequest request) {
        DocumentRecord doc = vaultService.initiateDocumentUpload(request);
        return ResponseEntity.ok(s3PostPolicyService.generatePostPolicy(doc.getS3ObjectKey(), request.contentType()));
    }

    // ==========================================
    // 🧠 3. SECURE METADATA RETRIEVAL ENGINE
    // ==========================================
    @Operation(summary = "One-line description of this endpoint")
    @GetMapping({"/applications/{applicationId}/documents", "/documents/{applicationId}"})
    public ResponseEntity<List<DocumentMetadataResponse>> applicationDocuments(@PathVariable String applicationId) {
        log.info("Vault Gateway: Retrieving document metadata matrix for Application {}", applicationId);
        return ResponseEntity.ok(vaultService.getApplicationDocuments(applicationId));
    }

    // ==========================================
    // 🧠 4. ZERO-TRUST BINARY STREAMING GATEWAY (NEW)
    // ==========================================
    @Operation(summary = "Secure Document Download Gateway")
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<?> downloadDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {

        // 1. Mandatory Security Check
        extractUserId(authentication);

        log.info("Vault Gateway: Requesting secure binary stream for Document {}", documentId);

        // 2. Fetch the metadata to correctly set HTTP Headers
        DocumentRecord metadata = vaultService.getDocumentMetadata(documentId);

        // 3. Failproof S3 Redirect
        if (metadata.getS3ObjectKey() != null && metadata.getS3ObjectKey().contains("/")) {
            S3PresignedUrlService.PresignedUrlResponse response = s3PresignedUrlService.generateDownloadUrl(metadata.getS3ObjectKey());
            return ResponseEntity.ok(java.util.Map.of("url", response.uploadUrl()));
        }

        // 4. Initiate the Byte-Stream (Local Fallback)
        Resource resource = vaultService.loadDocumentAsResource(documentId);

        // 5. Pipe directly to HTTP Response
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Securely deletes a document")
    @DeleteMapping("/documents/{applicationId}/{docType}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String applicationId,
            @PathVariable String docType,
            Authentication authentication) {
        
        extractUserId(authentication); // Mandatory Security Check
        
        log.info("Vault Gateway: Requesting document deletion for Application {} Document Type {}", applicationId, docType);
        
        vaultService.deleteDocument(applicationId, docType);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Securely deletes a document by ID")
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocumentById(
            @PathVariable UUID documentId,
            Authentication authentication) {
        
        extractUserId(authentication); // Mandatory Security Check
        
        log.info("Vault Gateway: Requesting document deletion for Document ID {}", documentId);
        
        vaultService.deleteDocumentById(documentId);
        
        return ResponseEntity.noContent().build();
    }
}
