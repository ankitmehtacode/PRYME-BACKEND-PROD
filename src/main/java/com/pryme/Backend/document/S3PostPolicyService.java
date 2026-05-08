package com.pryme.Backend.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 S3 POST POLICY ENGINE — EDGE-ENFORCED UPLOAD CONSTRAINTS
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * WHY THIS EXISTS:
 * A standard PUT presigned URL cannot enforce content-length-range from the server.
 * A malicious client can take that URL and upload a 5GB .mkv file, causing:
 *   1. S3 storage cost explosion
 *   2. Security alerts from anomalous object sizes
 *   3. Potential denial-of-service via bandwidth saturation
 *
 * HOW IT WORKS:
 * An S3 POST policy is a JSON document that gets Base64-encoded and HMAC-signed
 * with AWS SigV4. It contains cryptographic conditions like:
 *   - content-length-range: [0, 5242880]   → AWS rejects uploads > 5MB AT THE EDGE
 *   - Content-Type: application/pdf         → AWS rejects wrong MIME types
 *   - key: PRYME-3AE81F98/abc-123           → AWS rejects writes to other keys
 *
 * NOTE ON SDK VERSION:
 * AWS SDK v2 2.25.63 does NOT include S3Presigner.presignPostObject() — that API
 * was added in 2.31.x. The S3 presigner model at 2.25.63 only supports Get, Put,
 * Delete, and Multipart operations (verified via jar tf on the actual artifact).
 * Therefore, we implement SigV4 POST policy signing using standard JCE primitives.
 * When the SDK is upgraded past 2.31.x, this can be replaced with the native API.
 *
 * @since 2026-04-30
 */
@Service
@Slf4j
public class S3PostPolicyService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png"
    );

    /** 5MB — hard ceiling for KYC/financial documents. */
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    /** 5-minute TTL — reduced attack surface for URL replay. */
    private static final Duration POLICY_TTL = Duration.ofMinutes(5);

    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String SERVICE = "s3";

    private final AwsS3Properties awsS3Properties;

    @Value("${aws.accessKeyId:#{null}}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:#{null}}")
    private String secretAccessKey;

    public S3PostPolicyService(AwsS3Properties awsS3Properties) {
        this.awsS3Properties = awsS3Properties;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a cryptographically signed S3 POST policy for edge-enforced uploads.
     *
     * @param s3ObjectKey  the target S3 key (e.g., "PRYME-3AE81F98/abc-123")
     * @param contentType  the MIME type (must be pdf, jpeg, or png)
     * @return             PostPolicyResponse containing the endpoint URL and all signed form fields
     */
    public PostPolicyResponse generatePostPolicy(String s3ObjectKey, String contentType) {
        String normalizedType = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedType)) {
            throw new S3PresignedUrlService.DocumentTypeNotAllowedException(
                    "Unsupported contentType. Allowed: application/pdf, image/jpeg, image/png");
        }

        // Dummy mode for local/dev
        if ("dummy_bucket".equals(awsS3Properties.bucket()) ||
                accessKeyId == null || secretAccessKey == null) {
            log.warn("⚠️ Dummy S3 config — returning mock POST policy");
            return PostPolicyResponse.dummy(s3ObjectKey);
        }

        String region = awsS3Properties.region() != null ? awsS3Properties.region() : "ap-south-1";
        String bucket = awsS3Properties.bucket();

        Instant now = Instant.now();
        Instant expiration = now.plus(POLICY_TTL);

        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd")
                .withZone(ZoneOffset.UTC).format(now);
        String amzDate = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC).format(now);
        String credential = accessKeyId + "/" + dateStamp + "/" + region + "/" + SERVICE + "/aws4_request";

        // ── BUILD THE POLICY JSON ────────────────────────────────────────────
        String expirationStr = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneOffset.UTC).format(expiration);

        String policyJson = """
                {
                  "expiration": "%s",
                  "conditions": [
                    {"bucket": "%s"},
                    {"key": "%s"},
                    {"Content-Type": "%s"},
                    {"x-amz-algorithm": "%s"},
                    {"x-amz-credential": "%s"},
                    {"x-amz-date": "%s"},
                    ["content-length-range", 0, %d]
                  ]
                }
                """.formatted(expirationStr, bucket, s3ObjectKey, normalizedType,
                ALGORITHM, credential, amzDate, MAX_FILE_SIZE_BYTES);

        // ── SIGN THE POLICY ──────────────────────────────────────────────────
        String policyBase64 = Base64.getEncoder().encodeToString(
                policyJson.getBytes(StandardCharsets.UTF_8));

        byte[] signingKey = deriveSigningKey(dateStamp, region);
        String signature = hexEncode(hmacSha256(signingKey, policyBase64));

        // ── ASSEMBLE THE RESPONSE ────────────────────────────────────────────
        String endpoint = "https://" + bucket + ".s3." + region + ".amazonaws.com";

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("key", s3ObjectKey);
        fields.put("Content-Type", normalizedType);
        fields.put("x-amz-algorithm", ALGORITHM);
        fields.put("x-amz-credential", credential);
        fields.put("x-amz-date", amzDate);
        fields.put("policy", policyBase64);
        fields.put("x-amz-signature", signature);

        log.info("POST policy generated: key={} type={} maxBytes={} ttl={}s",
                s3ObjectKey, normalizedType, MAX_FILE_SIZE_BYTES, POLICY_TTL.toSeconds());

        return new PostPolicyResponse(endpoint, fields, s3ObjectKey, expiration);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AWS SIGV4 KEY DERIVATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Derives the SigV4 signing key:
     *   HMAC(HMAC(HMAC(HMAC("AWS4" + secretKey, dateStamp), region), service), "aws4_request")
     */
    private byte[] deriveSigningKey(String dateStamp, String region) {
        byte[] kSecret = ("AWS4" + secretAccessKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSha256(kSecret, dateStamp);
        byte[] kRegion = hmacSha256(kDate, region);
        byte[] kService = hmacSha256(kRegion, SERVICE);
        return hmacSha256(kService, "aws4_request");
    }

    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 computation failed", e);
        }
    }

    private static String hexEncode(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESPONSE DTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Contains the S3 POST endpoint and all cryptographically signed form fields.
     * The frontend builds a FormData, appends each field, then appends the file LAST.
     *
     * @param endpoint    S3 bucket URL (e.g., "https://gopryme-bucket.s3.ap-south-1.amazonaws.com")
     * @param fields      signed form fields — MUST be sent in order, file MUST be last
     * @param documentId  the S3 object key (used to correlate with DocumentRecord)
     * @param expiresAt   UTC expiration of the policy
     */
    public record PostPolicyResponse(
            String endpoint,
            Map<String, String> fields,
            String documentId,
            Instant expiresAt
    ) {
        static PostPolicyResponse dummy(String key) {
            return new PostPolicyResponse(
                    "/api/v1/dummy-s3-upload/" + key,
                    Map.of("key", key),
                    key,
                    Instant.now().plus(POLICY_TTL)
            );
        }
    }
}
