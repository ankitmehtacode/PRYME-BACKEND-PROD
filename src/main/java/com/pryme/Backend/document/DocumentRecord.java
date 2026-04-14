package com.pryme.Backend.document;

import com.pryme.Backend.crm.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_records", indexes = {
        @Index(name = "idx_doc_application", columnList = "loan_application_id"),
        @Index(name = "idx_doc_checksum", columnList = "checksum")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private String docType;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false, unique = true)
    private String storagePath;

    @Column(nullable = false, length = 64)
    private String checksum;

    @Column(nullable = false)
    private UUID uploadedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.AWAITING_UPLOAD;

    @Column
    private Instant uploadedAt;

    @Column(name = "s3_object_key")
    private String s3ObjectKey;


    public enum DocumentStatus {
        AWAITING_UPLOAD,
        UPLOADED,
        FAILED
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
