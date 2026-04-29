package com.pryme.Backend.document;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DummyS3Controller {

    private static final Logger log = LoggerFactory.getLogger(DummyS3Controller.class);
    private final DocumentVaultService vaultService;

    /**
     * 🧠 FAILPROOF ARCHITECTURE: Simulates AWS S3 PUT endpoint and SNS Webhook internally.
     * When operating with the 'dummy_bucket' configuration, the frontend will PUT the file
     * directly to this endpoint instead of AWS. This instantly triggers the webhook
     * reconciliation to transition the document from AWAITING_UPLOAD to UPLOADED.
     */
    @PutMapping("/dummy-s3-upload/**")
    public ResponseEntity<Void> dummyUpload(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String prefix = "/api/v1/dummy-s3-upload/";
        int idx = requestURI.indexOf(prefix);
        if (idx == -1) {
            return ResponseEntity.badRequest().build();
        }
        
        String objectKey = requestURI.substring(idx + prefix.length());
        log.info("Dummy S3 Gateway: Received simulated binary stream for object {}", objectKey);
        
        try {
            // Trigger the same internal workflow that the SNS Webhook Controller uses
            vaultService.markAsUploaded(objectKey);
            log.info("Dummy S3 Gateway: Successfully simulated SNS Webhook reconciliation for {}", objectKey);
        } catch (Exception e) {
            log.error("Dummy S3 Gateway: Webhook reconciliation failed", e);
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
}
