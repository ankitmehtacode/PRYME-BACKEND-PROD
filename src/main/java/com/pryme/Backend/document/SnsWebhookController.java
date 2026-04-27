package com.pryme.Backend.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/internal/webhooks")
@RequiredArgsConstructor
public class SnsWebhookController {

    private static final List<String> ALLOWED_CERT_HOST_SUFFIXES = List.of(".amazonaws.com", ".amazonaws.com.cn");

    private final DocumentVaultService documentVaultService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${app.webhooks.sns.shared-secret:dummy_webhook_secret_for_testing}")
    private String sharedSecret;

    @PostMapping("/s3-event")
    public ResponseEntity<Void> receiveS3Event(
            @RequestHeader("x-amz-sns-message-type") String snsMessageType,
            @org.springframework.web.bind.annotation.RequestParam(value = "secret", required = false) String providedSecret,
            @RequestBody String payload
    ) {
        if (!StringUtils.hasText(sharedSecret) || !sharedSecret.equals(providedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            JsonNode snsEnvelope = objectMapper.readTree(payload);
            if (!isSignatureValid(snsEnvelope)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if ("SubscriptionConfirmation".equals(snsMessageType)) {
                String subscribeUrl = snsEnvelope.path("SubscribeURL").asText();
                if (!isTrustedSnsUrl(subscribeUrl)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                restClient.get().uri(URI.create(subscribeUrl)).retrieve().toBodilessEntity();
                return ResponseEntity.ok().build();
            }

            if ("Notification".equals(snsMessageType)) {
                JsonNode messageNode = objectMapper.readTree(snsEnvelope.path("Message").asText());
                String objectKey = messageNode.path("Records").path(0).path("s3").path("object").path("key").asText();
                documentVaultService.markAsUploaded(objectKey);
            }

            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private boolean isSignatureValid(JsonNode snsEnvelope) {
        try {
            String certUrl = snsEnvelope.path("SigningCertURL").asText();
            if (!isTrustedSigningCertUrl(certUrl)) {
                return false;
            }

            String certPem = restClient.get().uri(URI.create(certUrl)).retrieve().body(String.class);
            X509Certificate certificate = parseCertificate(certPem);
            PublicKey publicKey = certificate.getPublicKey();

            String stringToSign = buildStringToSign(snsEnvelope);

            String signatureVersion = snsEnvelope.path("SignatureVersion").asText("1");
            String algorithm = "2".equals(signatureVersion) ? "SHA256withRSA" : "SHA1withRSA";

            Signature verifier = Signature.getInstance(algorithm);
            verifier.initVerify(publicKey);
            verifier.update(stringToSign.getBytes(StandardCharsets.UTF_8));
            byte[] signature = Base64.getDecoder().decode(snsEnvelope.path("Signature").asText());
            return verifier.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String buildStringToSign(JsonNode envelope) {
        String type = envelope.path("Type").asText();
        StringBuilder sb = new StringBuilder();

        appendField(sb, "Message", envelope.path("Message").asText(null));
        appendField(sb, "MessageId", envelope.path("MessageId").asText(null));

        if ("Notification".equals(type)) {
            appendField(sb, "Subject", envelope.path("Subject").asText(null));
        }

        appendField(sb, "Timestamp", envelope.path("Timestamp").asText(null));
        appendField(sb, "TopicArn", envelope.path("TopicArn").asText(null));
        appendField(sb, "Type", type);

        if ("SubscriptionConfirmation".equals(type) || "UnsubscribeConfirmation".equals(type)) {
            appendField(sb, "SubscribeURL", envelope.path("SubscribeURL").asText(null));
            appendField(sb, "Token", envelope.path("Token").asText(null));
        }

        return sb.toString();
    }

    private void appendField(StringBuilder builder, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        builder.append(key).append('\n').append(value).append('\n');
    }

    private boolean isTrustedSigningCertUrl(String certUrl) {
        URI uri = URI.create(certUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        String host = uri.getHost();
        return host != null && ALLOWED_CERT_HOST_SUFFIXES.stream().anyMatch(host::endsWith);
    }

    private boolean isTrustedSnsUrl(String url) {
        URI uri = URI.create(url);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        String host = uri.getHost();
        return host != null && ALLOWED_CERT_HOST_SUFFIXES.stream().anyMatch(host::endsWith);
    }

    private X509Certificate parseCertificate(String certPem) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(
                new java.io.ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8))
        );
    }
}
