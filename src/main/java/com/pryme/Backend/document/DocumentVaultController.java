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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1") // 🧠 Top-level routing to support both RESTful and RPC paths
@RequiredArgsConstructor
public class DocumentVaultController {

    private static final Logger log = LoggerFactory.getLogger(DocumentVaultController.class);
    private final DocumentVaultService vaultService;

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

    // ==========================================
    // 🧠 2. MULTIPART INGESTION ENGINE (POLYMORPHIC)
    // ==========================================
    @PostMapping(value = {
            "/applications/{applicationId}/documents",
            "/documents/upload"
    }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @PathVariable(required = false) String applicationId,
            @RequestParam(value = "applicationId", required = false) String queryAppId,
            @RequestParam(value = "docType", required = false) String docType,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam("file") MultipartFile file
    ) {
        String targetAppId = (applicationId != null) ? applicationId : queryAppId;
        String targetDocType = (docType != null) ? docType : documentName;

        if (targetAppId == null || targetDocType == null) {
            log.error("Vault Gateway: Missing routing parameters. AppId: {}, DocType: {}", targetAppId, targetDocType);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Critical payload missing: applicationId and docType/documentName are strictly required."
            ));
        }

        log.info("Vault Gateway: Ingesting '{}' for Application {}", targetDocType, targetAppId);

        DocumentMetadataResponse result = vaultService.securelyStoreDocument(targetAppId, targetDocType, file);

        return ResponseEntity.ok(Map.of(
                "status", "STORED",
                "document", result,
                "message", "Document stored securely in the Vault."
        ));
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