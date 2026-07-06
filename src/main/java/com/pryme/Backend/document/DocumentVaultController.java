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
    @Operation(summary = "Verify applicant identity signature")
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
    public ResponseEntity<Map<String, Object>> initiateUpload(@Valid @RequestBody DocumentUploadRequest request) {
        DocumentRecord doc = vaultService.initiateDocumentUpload(request);
        S3PresignedUrlService.PresignedUrlResponse presigned = s3PresignedUrlService.generateUploadUrl(doc.getS3ObjectKey(), request.contentType());
        // 🧠 CRITICAL: Return the actual document UUID separately from the S3 key.
        // The presigned response's 'documentId' is the S3 object key (e.g., "PRYME-XXX/uuid"),
        // but confirm-upload needs the raw UUID. We merge both into one response.
        return ResponseEntity.ok(Map.of(
                "uploadUrl", presigned.uploadUrl(),
                "documentId", doc.getId().toString(),
                "s3ObjectKey", presigned.documentId(),
                "expiresAt", presigned.expiresAt().toString()
        ));
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
    @Operation(summary = "Retrieve document metadata for an application")
    @GetMapping({"/applications/{applicationId}/documents", "/documents/{applicationId}"})
    public ResponseEntity<List<DocumentMetadataResponse>> applicationDocuments(@PathVariable String applicationId) {
        log.info("Vault Gateway: Retrieving document metadata matrix for Application {}", applicationId);
        return ResponseEntity.ok(vaultService.getApplicationDocuments(applicationId));
    }

    // ==========================================
    // 🧠 4. ZERO-TRUST BINARY STREAMING GATEWAY
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

        // 3. STATUS GUARD & SELF-HEALING:
        //    For historical documents uploaded before the confirm-upload webhook was introduced,
        //    they might be stuck in AWAITING_UPLOAD status even if the file exists in S3.
        //    We check S3. If it exists, we self-heal the DB record. If not, we block the download.
        boolean s3ExistenceVerified = false;
        if (metadata.getStatus() == DocumentRecord.DocumentStatus.AWAITING_UPLOAD) {
            String s3Key = metadata.getS3ObjectKey();
            if (s3Key != null && !s3Key.isBlank() && s3PresignedUrlService.objectExists(s3Key)) {
                log.info("🏥 Self-Healing: Document {} exists in S3 key '{}' but status was AWAITING_UPLOAD. Transitioning to UPLOADED.", documentId, s3Key);
                // Controller already verified S3 existence — use lightweight transition to avoid redundant HeadObject
                vaultService.transitionToUploaded(documentId);
                metadata = vaultService.getDocumentMetadata(documentId);
                s3ExistenceVerified = true; // Skip redundant probe in step 4
            } else {
                log.warn("Download blocked: Document {} is still AWAITING_UPLOAD and does not exist in S3.", documentId);
                return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                        .body(java.util.Map.of(
                                "error", "DOCUMENT_NOT_UPLOADED",
                                "message", "This document is still awaiting upload. Please upload the file first."
                        ));
            }
        }

        // 4. Failproof S3 Redirect with existence verification
        if (metadata.getS3ObjectKey() != null && metadata.getS3ObjectKey().contains("/")) {
            // 🧠 EXISTENCE PROBE: Only call HeadObject if we haven't already verified in step 3.
            // On the self-healing path, the guard already confirmed the object exists — no redundant call.
            if (!s3ExistenceVerified && !s3PresignedUrlService.objectExists(metadata.getS3ObjectKey())) {
                log.error("🚨 S3 object missing for Document {} at key '{}'. Possible upload failure.", documentId, metadata.getS3ObjectKey());
                return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of(
                                "error", "S3_OBJECT_MISSING",
                                "message", "The document file could not be found in the secure vault. It may need to be re-uploaded."
                        ));
            }

            S3PresignedUrlService.PresignedUrlResponse response = s3PresignedUrlService.generateDownloadUrl(metadata.getS3ObjectKey());
            return ResponseEntity.ok(java.util.Map.of("url", response.uploadUrl()));
        }

        // 5. Initiate the Byte-Stream (Local Fallback)
        Resource resource = vaultService.loadDocumentAsResource(documentId);

        // 6. Pipe directly to HTTP Response
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }

    // ==========================================
    // 🧠 5. CLOSED-LOOP UPLOAD CONFIRMATION
    // ==========================================
    @Operation(summary = "Confirm a document upload after S3 PUT succeeds")
    @PostMapping("/documents/{documentId}/confirm-upload")
    public ResponseEntity<java.util.Map<String, String>> confirmUpload(
            @PathVariable UUID documentId,
            Authentication authentication) {

        extractUserId(authentication); // Mandatory Security Check

        log.info("Vault Gateway: Upload confirmation requested for Document {}", documentId);

        vaultService.confirmUpload(documentId);

        return ResponseEntity.ok(java.util.Map.of(
                "status", "CONFIRMED",
                "message", "Document upload verified and confirmed."
        ));
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
