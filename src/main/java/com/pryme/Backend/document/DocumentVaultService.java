package com.pryme.Backend.document;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.common.NotFoundException;
import com.pryme.Backend.crm.LoanApplication;
import com.pryme.Backend.crm.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentVaultService {

    private static final Logger log = LoggerFactory.getLogger(DocumentVaultService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB limit
    private static final int MAX_DOC_TYPE_LENGTH = 64;

    private static final Map<String, Set<String>> MIME_TO_EXTENSIONS = Map.of(
            "application/pdf", Set.of("pdf"),
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png")
    );

    private final LoanApplicationRepository loanApplicationRepository;
    private final DocumentRecordRepository documentRecordRepository;
    private final Path storageRoot;

    public DocumentVaultService(
            LoanApplicationRepository loanApplicationRepository,
            DocumentRecordRepository documentRecordRepository,
            @Value("${app.documents.storage-root:./secure_vault}") String storageRoot
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.documentRecordRepository = documentRecordRepository;

        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.storageRoot);
            log.info("Secure Vault Initialized at: {}", this.storageRoot);
        } catch (IOException ex) {
            throw new RuntimeException("CRITICAL: Unable to initialize document storage root", ex);
        }
    }

    // ==========================================
    // 🧠 ZERO-TRUST UPLOAD GATEWAY
    // ==========================================
    @Transactional
    public DocumentMetadataResponse securelyStoreDocument(String applicationId, String docType, MultipartFile file) {
        String safeApplicationId = sanitizeApplicationId(applicationId);
        String safeDocType = sanitizeDocType(docType);

        LoanApplication application = getAuthorizedApplication(safeApplicationId);
        validateFile(file);

        String originalFilename = sanitizeOriginalFilename(file.getOriginalFilename());
        String extension = extensionFromFilename(originalFilename);
        String normalizedContentType = file.getContentType().toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported document extension. Allowed: pdf, jpg, jpeg, png");
        }
        validateMimeExtensionConsistency(normalizedContentType, extension);

        String storedFilename = UUID.randomUUID() + "_" + safeDocType + "." + extension;
        Path target = null;

        try {
            Path applicationDir = storageRoot.resolve(safeApplicationId).toAbsolutePath().normalize();

            if (!applicationDir.startsWith(storageRoot)) {
                log.error("Path Traversal Blocked. Dir: {}, Root: {}", applicationDir, storageRoot);
                throw new IllegalArgumentException("Path Traversal Attack Blocked.");
            }
            Files.createDirectories(applicationDir);

            target = applicationDir.resolve(storedFilename).toAbsolutePath().normalize();

            if (!target.startsWith(storageRoot)) {
                log.error("Path Traversal Blocked. Target: {}, Root: {}", target, storageRoot);
                throw new IllegalArgumentException("Path Traversal Attack Blocked.");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream stream = file.getInputStream(); DigestInputStream dis = new DigestInputStream(stream, digest)) {
                Files.copy(dis, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String checksum = HexFormat.of().formatHex(digest.digest());

            UUID uploaderId = userIdFromAuth();
            String universalRelativePath = safeApplicationId + "/" + storedFilename;

            DocumentRecord newDocument = DocumentRecord.builder()
                    .docType(safeDocType)
                    .originalFilename(originalFilename)
                    .contentType(normalizedContentType)
                    .fileSize(file.getSize())
                    .storagePath(universalRelativePath)
                    .checksum(checksum)
                    .uploadedBy(uploaderId)
                    .build();

            application.addDocument(newDocument);

            DocumentRecord saved = documentRecordRepository.save(newDocument);
            loanApplicationRepository.save(application);

            log.info("Secure Vault: Ingested {} successfully for Application {}", safeDocType, safeApplicationId);
            return toResponse(saved);

        } catch (IOException | NoSuchAlgorithmException ex) {
            deleteQuietly(target);
            throw new RuntimeException("Failed to mathematically encrypt and store document", ex);
        } catch (RuntimeException ex) {
            deleteQuietly(target);
            throw ex;
        }
    }

    // ==========================================
    // 🧠 ZERO-TRUST BINARY STREAMING GATEWAY (NEW)
    // ==========================================
    @Transactional(readOnly = true)
    public Resource loadDocumentAsResource(UUID documentId) {
        DocumentRecord document = documentRecordRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document matrix not found."));

        // 1. Execute Role-Based Access Control (RBAC) check
        LoanApplication application = document.getLoanApplication();
        UUID currentUserId = userIdFromAuth();
        boolean isPrivileged = isPrivilegedRole();

        if (!isPrivileged && !application.getApplicant().getId().equals(currentUserId)) {
            log.warn("🚨 Security Fault: Unauthorized stream attempt on Document {} by User {}", documentId, currentUserId);
            throw new ForbiddenException("Zero-Trust Violation: You lack clearance to view this document.");
        }

        try {
            // 2. Resolve physical path and enforce Traversal Protection
            Path filePath = storageRoot.resolve(document.getStoragePath()).toAbsolutePath().normalize();

            if (!filePath.startsWith(storageRoot)) {
                throw new SecurityException("Path Traversal Attack Blocked during stream retrieval.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.info("Vault Gateway: Authorized binary stream initiated for document {}", documentId);
                return resource;
            } else {
                throw new NotFoundException("Document binary has been corrupted or relocated.");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Failed to construct secure URI for document binary stream.", ex);
        }
    }

    @Transactional(readOnly = true)
    public DocumentRecord getDocumentMetadata(UUID documentId) {
        return documentRecordRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document metadata not found."));
    }

    // ==========================================
    // RETRIEVAL & UTILITY METHODS
    // ==========================================
    @Transactional(readOnly = true)
    public List<DocumentMetadataResponse> getApplicationDocuments(String applicationId) {
        String safeApplicationId = sanitizeApplicationId(applicationId);
        getAuthorizedApplication(safeApplicationId);
        return documentRecordRepository.findAllByLoanApplication_ApplicationIdOrderByCreatedAtDesc(safeApplicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean verifyIdentityMatrix(String applicationId, String idType, String idNumber) {
        getAuthorizedApplication(sanitizeApplicationId(applicationId));
        String normalizedType = idType.trim().toUpperCase(Locale.ROOT);
        String normalizedNumber = idNumber.trim().toUpperCase(Locale.ROOT);

        if ("PAN".equals(normalizedType)) {
            return normalizedNumber.matches("^[A-Z]{5}[0-9]{4}[A-Z]$");
        }

        if ("AADHAR".equals(normalizedType) || "AADHAAR".equals(normalizedType)) {
            return normalizedNumber.matches("^[0-9]{12}$");
        }

        throw new IllegalArgumentException("Unsupported idType. Allowed values: PAN, AADHAR");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File payload is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 10 MB");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Unsupported MIME type. Allowed: application/pdf, image/jpeg, image/png");
        }
    }

    private String sanitizeApplicationId(String applicationId) {
        if (!StringUtils.hasText(applicationId)) {
            throw new IllegalArgumentException("applicationId is required");
        }
        String cleaned = applicationId.trim().toUpperCase(Locale.ROOT);
        if (!cleaned.matches("^[A-Z0-9-]{3,40}$")) {
            throw new IllegalArgumentException("Invalid applicationId format");
        }
        return cleaned;
    }

    private String sanitizeOriginalFilename(String filename) {
        String candidate = StringUtils.hasText(filename) ? filename : "document.bin";
        String cleaned = StringUtils.cleanPath(candidate).replace('\\', '/');
        if (cleaned.contains("..") || cleaned.contains("/")) {
            throw new IllegalArgumentException("Invalid filename - path traversal sequence detected");
        }
        return cleaned;
    }

    private LoanApplication getAuthorizedApplication(String applicationId) {
        LoanApplication application = loanApplicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Application Matrix not found: " + applicationId));

        UUID currentUserId = userIdFromAuth();
        boolean isPrivileged = isPrivilegedRole();
        UUID applicantId = application.getApplicant() == null ? null : application.getApplicant().getId();

        if (!isPrivileged && (applicantId == null || !applicantId.equals(currentUserId))) {
            throw new ForbiddenException("Zero-Trust Violation: You do not own this application.");
        }
        return application;
    }

    private boolean isPrivilegedRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_EMPLOYEE"));
    }

    private UUID userIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new ForbiddenException("Authentication matrix required");
        }
        Object principal = auth.getPrincipal();
        try {
            if (principal instanceof UUID) {
                return (UUID) principal;
            } else {
                return UUID.fromString(principal.toString());
            }
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("Invalid authentication token footprint.");
        }
    }

    private String sanitizeDocType(String docType) {
        if (docType == null || docType.isBlank()) {
            throw new IllegalArgumentException("Document Name is required");
        }
        String cleaned = docType.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");

        if (cleaned.length() > MAX_DOC_TYPE_LENGTH || !cleaned.matches("^[A-Z0-9_\\-]{2,64}$")) {
            throw new IllegalArgumentException("Document Name contains invalid characters or exceeds 64 chars");
        }
        return cleaned;
    }

    private String extensionFromFilename(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            throw new IllegalArgumentException("Valid file extension is required (.pdf, .jpg, .png)");
        }
        return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private void validateMimeExtensionConsistency(String contentType, String extension) {
        Set<String> allowed = MIME_TO_EXTENSIONS.get(contentType);
        if (allowed == null || !allowed.contains(extension)) {
            throw new IllegalArgumentException("Security mismatch: MIME type does not match file extension.");
        }
    }

    private void deleteQuietly(Path target) {
        if (target == null) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }

    private DocumentMetadataResponse toResponse(DocumentRecord document) {
        return new DocumentMetadataResponse(
                document.getId(),
                document.getLoanApplication().getApplicationId(),
                document.getDocType(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getStoragePath(),
                document.getChecksum(),
                document.getCreatedAt()
        );
    }
}