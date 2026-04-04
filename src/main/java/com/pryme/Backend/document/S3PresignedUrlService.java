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

    @Bean
    S3Presigner s3Presigner(AwsS3Properties awsS3Properties) {
        return S3Presigner.builder()
                .region(Region.of(awsS3Properties.region()))
                .build();
    }
}
