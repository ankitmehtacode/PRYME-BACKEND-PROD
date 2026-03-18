package com.pryme.Backend.document;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1") // 🧠 Top-level routing to support both RESTful and RPC paths
public class DocumentVaultController {

    private static final Logger log = LoggerFactory.getLogger(DocumentVaultController.class);
    private final DocumentVaultService vaultService;

    public DocumentVaultController(DocumentVaultService vaultService) {
        this.vaultService = vaultService;
    }

    // ==========================================
    // 🧠 1. IDENTITY VERIFICATION GATEWAY
    // ==========================================
    @PostMapping("/documents/verify-id")
    public ResponseEntity<Map<String, String>> verifyIdentity(@Valid @RequestBody VerifyIdRequest request) {
        log.info("Vault Gateway: Verifying Identity Matrix for Application {}", request.applicationId());

        boolean verified = vaultService.verifyIdentityMatrix(request.applicationId(), request.idType(), request.idNumber());

        if (!verified) {
            // Graceful rejection prevents ugly 500 stack traces in the backend logs
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
    // Supports both RESTful and Legacy RPC paths to guarantee frontend compatibility
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
        // 🧠 Elastic Parameter Resolution:
        // Adapts instantly based on how the React frontend was generated
        String targetAppId = (applicationId != null) ? applicationId : queryAppId;
        String targetDocType = (docType != null) ? docType : documentName;

        if (targetAppId == null || targetDocType == null) {
            log.error("Vault Gateway: Missing routing parameters. AppId: {}, DocType: {}", targetAppId, targetDocType);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Critical payload missing: applicationId and docType/documentName are strictly required."
            ));
        }

        log.info("Vault Gateway: Ingesting '{}' for Application {}", targetDocType, targetAppId);

        // The service handles the Zero-Trust ownership check via SecurityContextHolder
        DocumentMetadataResponse result = vaultService.securelyStoreDocument(targetAppId, targetDocType, file);

        return ResponseEntity.ok(Map.of(
                "status", "STORED",
                "document", result,
                "message", "Document stored securely in the Vault."
        ));
    }

    // ==========================================
    // 🧠 3. SECURE RETRIEVAL ENGINE
    // ==========================================
    @GetMapping({"/applications/{applicationId}/documents", "/documents/{applicationId}"})
    public ResponseEntity<List<DocumentMetadataResponse>> applicationDocuments(@PathVariable String applicationId) {
        log.info("Vault Gateway: Retrieving document matrix for Application {}", applicationId);
        return ResponseEntity.ok(vaultService.getApplicationDocuments(applicationId));
    }
}