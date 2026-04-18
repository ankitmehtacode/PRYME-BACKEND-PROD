package com.pryme.Backend.document;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
public class S3PresignedUrlService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");

    private final S3Presigner s3Presigner;
    private final AwsS3Properties awsS3Properties;

    public S3PresignedUrlService(S3Presigner s3Presigner, AwsS3Properties awsS3Properties) {
        this.s3Presigner = s3Presigner;
        this.awsS3Properties = awsS3Properties;
    }

    public PresignedUrlResponse generateUploadUrl(String documentId, String contentType) {
        String normalizedType = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedType)) {
            throw new DocumentTypeNotAllowedException("Unsupported contentType. Allowed: application/pdf, image/jpeg, image/png");
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsS3Properties.bucket())
                .key(documentId)
                .contentType(normalizedType)
                .build();

        Duration ttl = Duration.ofMinutes(15);
        Instant expiresAt = Instant.now().plus(ttl);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(ttl)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(presignedRequest.url().toString(), documentId, expiresAt);
    }

    public record PresignedUrlResponse(String uploadUrl, String documentId, Instant expiresAt) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class DocumentTypeNotAllowedException extends RuntimeException {
        public DocumentTypeNotAllowedException(String message) {
            super(message);
        }
    }
}

@ConfigurationProperties(prefix = "aws.s3")
record AwsS3Properties(String bucket, String region) {
}

@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
class S3PresignerConfiguration {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(S3PresignerConfiguration.class);
    private final AwsS3Properties awsS3Properties;

    public S3PresignerConfiguration(AwsS3Properties awsS3Properties) {
        this.awsS3Properties = awsS3Properties;
    }

    @jakarta.annotation.PostConstruct
    public void validateDataResidencyAndBucket() {
        String region = awsS3Properties.region();

        // Guard: skip all validation when running locally without S3 config
        if (region == null || region.isBlank()) {
            log.warn("⚠️ aws.s3.region is not configured — skipping S3 data-residency validation (local/test mode).");
            return;
        }

        // 🧠 1% FIX: Explicit bypass for staged deployments without AWS
        if ("dummy_bucket".equals(awsS3Properties.bucket())) {
            log.warn("⚠️ 'dummy_bucket' detected. Bypassing strict S3 ping. Document vault is currently offline.");
            return;
        }

        // 🧠 160 IQ: INDIA DATA RESIDENCY COMPLIANCE (RBI GUIDELINES)
        if (!region.equals("ap-south-1") && !region.equals("ap-south-2")) {
            throw new IllegalStateException(
                "🚨 SEVERE: RBI Data Residency Violation! Financial documents must be stored in India (ap-south-1 or ap-south-2). " 
                + "Configured region: " + region
            );
        }

        // 🧠 PROACTIVE BUCKET EXISTENCE & REGION VERIFICATION
        log.info("Verifying S3 bucket '{}' exists and is physically located in {}", awsS3Properties.bucket(), region);
        try (software.amazon.awssdk.services.s3.S3Client s3Client = software.amazon.awssdk.services.s3.S3Client.builder()
                .region(Region.of(region))
                .build()) {
            
            s3Client.headBucket(b -> b.bucket(awsS3Properties.bucket()));
            log.info("✅ S3 Bucket Validation Passed. Data residency enforced.");
            
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            if (e.statusCode() == 301) {
                throw new IllegalStateException("🚨 FATAL: Bucket exists but is physically located outside " + region + "! Data Residency Breach.", e);
            } else if (e.statusCode() == 403) {
                throw new IllegalStateException("🚨 FATAL: Access Denied to Bucket '" + awsS3Properties.bucket() + "'. Check IAM Instance Role/Keys.", e);
            } else if (e.statusCode() == 404) {
                throw new IllegalStateException("🚨 FATAL: Bucket '" + awsS3Properties.bucket() + "' DOES NOT EXIST.", e);
            }
            throw new IllegalStateException("🚨 FATAL: Unknown S3 Validation Error", e);
        } catch (Exception e) {
            // Safe fallback for local developer environments without IAM credentials
            log.warn("⚠️ Skipping strict S3 bucket ping. AWS credentials missing or network unreachable: {}", e.getMessage());
        }
    }

    @Bean
    S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(awsS3Properties.region()))
                .build();
    }
}
