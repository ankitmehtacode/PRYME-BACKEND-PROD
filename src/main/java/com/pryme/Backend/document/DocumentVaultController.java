package com.pryme.Backend.document;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentVaultController {

    private final DocumentVaultService vaultService;

    public DocumentVaultController(DocumentVaultService vaultService) {
        this.vaultService = vaultService;
    }

    @PostMapping("/verify-id")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Map<String, String>> verifyIdentity(@Valid @RequestBody VerifyIdRequest request) {
        boolean verified = vaultService.verifyIdentityMatrix(request.applicationId(), request.idType(), request.idNumber());
        if (!verified) {
            throw new IllegalArgumentException("Identity number did not pass verification");
        }
        return ResponseEntity.ok(Map.of(
                "status", "VERIFIED",
                "message", "Identity signature validated"
        ));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("applicationId") String applicationId,
            @RequestParam("docType") String docType,
            @RequestParam("file") MultipartFile file
    ) {
        DocumentMetadataResponse result = vaultService.securelyStoreDocument(applicationId, docType, file);
        return ResponseEntity.ok(Map.of(
                "status", "STORED",
                "document", result,
                "message", "Document stored securely"
        ));
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<DocumentMetadataResponse>> applicationDocuments(@PathVariable String applicationId) {
        return ResponseEntity.ok(vaultService.getApplicationDocuments(applicationId));
    }
}
