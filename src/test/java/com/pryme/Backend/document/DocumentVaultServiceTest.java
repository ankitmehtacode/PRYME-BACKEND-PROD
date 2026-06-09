package com.pryme.Backend.document;

import com.pryme.Backend.common.ForbiddenException;
import com.pryme.Backend.crm.LoanApplication;
import com.pryme.Backend.crm.LoanApplicationRepository;
import com.pryme.Backend.iam.Role;
import com.pryme.Backend.iam.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentVaultServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private DocumentRecordRepository documentRecordRepository;
    @Mock
    private S3PresignedUrlService s3PresignedUrlService;

    private Path tempDir;
    private DocumentVaultService service;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("vault-test");
        service = new DocumentVaultService(loanApplicationRepository, documentRecordRepository, s3PresignedUrlService, tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void securelyStoreDocumentStoresAllowedFile() {
        UUID userId = UUID.randomUUID();
        setUserAuth(userId, "ROLE_USER");

        LoanApplication application = LoanApplication.builder()
                .applicationId("PRY-1001")
                .applicant(User.builder().id(userId).role(Role.USER).build())
                .build();
        when(loanApplicationRepository.findByApplicationId("PRY-1001")).thenReturn(Optional.of(application));
        when(documentRecordRepository.save(any(DocumentRecord.class))).thenAnswer(invocation -> {
            DocumentRecord d = invocation.getArgument(0);
            d.setId(UUID.randomUUID());
            d.setLoanApplication(application);
            return d;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "kyc.pdf",
                "application/pdf",
                "sample-pdf".getBytes()
        );

        DocumentMetadataResponse response = service.securelyStoreDocument("PRY-1001", "KYC", file);

        assertEquals("PRY-1001", response.applicationId());
        assertEquals("KYC", response.docType());
        assertEquals(64, response.checksum().length());
        assertTrue(Files.exists(tempDir.resolve(response.storagePath())));
    }

    @Test
    void securelyStoreDocumentRejectsWrongMimeType() {
        UUID userId = UUID.randomUUID();
        setUserAuth(userId, "ROLE_USER");

        LoanApplication application = LoanApplication.builder()
                .applicationId("PRY-1001")
                .applicant(User.builder().id(userId).role(Role.USER).build())
                .build();
        when(loanApplicationRepository.findByApplicationId("PRY-1001")).thenReturn(Optional.of(application));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.sh",
                "text/plain",
                "rm -rf".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.securelyStoreDocument("PRY-1001", "KYC", file)
        );
        assertTrue(ex.getMessage().contains("Unsupported MIME type"));
    }

    @Test
    void securelyStoreDocumentRejectsMimeExtensionMismatch() {
        UUID userId = UUID.randomUUID();
        setUserAuth(userId, "ROLE_USER");

        LoanApplication application = LoanApplication.builder()
                .applicationId("PRY-1001")
                .applicant(User.builder().id(userId).role(Role.USER).build())
                .build();
        when(loanApplicationRepository.findByApplicationId("PRY-1001")).thenReturn(Optional.of(application));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "application/pdf",
                "fake".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.securelyStoreDocument("PRY-1001", "KYC", file)
        );
        assertTrue(ex.getMessage().contains("mismatch"));
    }

    @Test
    void securelyStoreDocumentRejectsTraversalFilename() {
        UUID userId = UUID.randomUUID();
        setUserAuth(userId, "ROLE_USER");

        LoanApplication application = LoanApplication.builder()
                .applicationId("PRY-1001")
                .applicant(User.builder().id(userId).role(Role.USER).build())
                .build();
        when(loanApplicationRepository.findByApplicationId("PRY-1001")).thenReturn(Optional.of(application));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../steal.pdf",
                "application/pdf",
                "payload".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.securelyStoreDocument("PRY-1001", "KYC", file)
        );
        assertTrue(ex.getMessage().contains("Invalid filename"));
    }

    @Test
    void getDocumentsBlocksUnauthorizedUser() {
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        setUserAuth(attackerId, "ROLE_USER");

        LoanApplication application = LoanApplication.builder()
                .applicationId("PRY-1001")
                .applicant(User.builder().id(ownerId).role(Role.USER).build())
                .build();
        when(loanApplicationRepository.findByApplicationId("PRY-1001")).thenReturn(Optional.of(application));

        assertThrows(ForbiddenException.class, () -> service.getApplicationDocuments("PRY-1001"));
    }

    @Test
    void confirmUploadSuccess() {
        UUID userId = UUID.randomUUID();
        setUserAuth(userId, "ROLE_USER");
        UUID documentId = UUID.randomUUID();

        LoanApplication application = LoanApplication.builder()
                .applicationId("PRY-1001")
                .applicant(User.builder().id(userId).role(Role.USER).build())
                .build();

        DocumentRecord doc = new DocumentRecord();
        doc.setId(documentId);
        doc.setLoanApplication(application);
        doc.setS3ObjectKey("PRY-1001/doc");
        doc.setStatus(DocumentRecord.DocumentStatus.AWAITING_UPLOAD);

        when(documentRecordRepository.findById(documentId)).thenReturn(Optional.of(doc));
        when(loanApplicationRepository.findByApplicationId("PRY-1001")).thenReturn(Optional.of(application));
        when(s3PresignedUrlService.objectExists("PRY-1001/doc")).thenReturn(true);

        assertDoesNotThrow(() -> service.confirmUpload(documentId));
        assertEquals(DocumentRecord.DocumentStatus.UPLOADED, doc.getStatus());
        assertNotNull(doc.getUploadedAt());
    }

    @Test
    void confirmUploadThrowsNotFoundException() {
        UUID documentId = UUID.randomUUID();
        when(documentRecordRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(com.pryme.Backend.common.NotFoundException.class, () -> service.confirmUpload(documentId));
    }

    @Test
    void confirmUploadThrowsIllegalStateExceptionWhenObjectMissing() {
        UUID userId = UUID.randomUUID();
        setUserAuth(userId, "ROLE_USER");
        UUID documentId = UUID.randomUUID();

        LoanApplication application = LoanApplication.builder()
                .applicationId("PRY-1001")
                .applicant(User.builder().id(userId).role(Role.USER).build())
                .build();

        DocumentRecord doc = new DocumentRecord();
        doc.setId(documentId);
        doc.setLoanApplication(application);
        doc.setS3ObjectKey("PRY-1001/doc");
        doc.setStatus(DocumentRecord.DocumentStatus.AWAITING_UPLOAD);

        when(documentRecordRepository.findById(documentId)).thenReturn(Optional.of(doc));
        when(loanApplicationRepository.findByApplicationId("PRY-1001")).thenReturn(Optional.of(application));
        when(s3PresignedUrlService.objectExists("PRY-1001/doc")).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.confirmUpload(documentId));
        assertTrue(ex.getMessage().contains("was not found"));
    }

    @Test
    void confirmUploadIdempotentNoOpWhenAlreadyUploaded() {
        UUID documentId = UUID.randomUUID();

        DocumentRecord doc = new DocumentRecord();
        doc.setId(documentId);
        doc.setStatus(DocumentRecord.DocumentStatus.UPLOADED);

        when(documentRecordRepository.findById(documentId)).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> service.confirmUpload(documentId));
        assertEquals(DocumentRecord.DocumentStatus.UPLOADED, doc.getStatus());
    }

    @Test
    void transitionToUploadedSuccess() {
        UUID documentId = UUID.randomUUID();

        DocumentRecord doc = new DocumentRecord();
        doc.setId(documentId);
        doc.setStatus(DocumentRecord.DocumentStatus.AWAITING_UPLOAD);

        when(documentRecordRepository.findById(documentId)).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> service.transitionToUploaded(documentId));
        assertEquals(DocumentRecord.DocumentStatus.UPLOADED, doc.getStatus());
        assertNotNull(doc.getUploadedAt());
        // Verify NO S3 interaction — the whole point of this method
        verify(s3PresignedUrlService, never()).objectExists(any());
    }

    @Test
    void transitionToUploadedIdempotent() {
        UUID documentId = UUID.randomUUID();

        DocumentRecord doc = new DocumentRecord();
        doc.setId(documentId);
        doc.setStatus(DocumentRecord.DocumentStatus.UPLOADED);

        when(documentRecordRepository.findById(documentId)).thenReturn(Optional.of(doc));

        assertDoesNotThrow(() -> service.transitionToUploaded(documentId));
        // Should NOT re-save since already uploaded
        verify(documentRecordRepository, never()).save(any());
    }

    @Test
    void transitionToUploadedThrowsNotFound() {
        UUID documentId = UUID.randomUUID();
        when(documentRecordRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(com.pryme.Backend.common.NotFoundException.class, () -> service.transitionToUploaded(documentId));
    }

    private static void setUserAuth(UUID userId, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                userId,
                "token",
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
