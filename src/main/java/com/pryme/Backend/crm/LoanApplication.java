package com.pryme.Backend.crm;

import com.pryme.Backend.document.DocumentRecord;
import com.pryme.Backend.iam.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "loan_applications",
        indexes = {
                // 🧠 PRODUCTION GRADE: Instant lookup speeds for CRM dashboards
                @Index(name = "idx_application_id", columnList = "applicationId", unique = true),
                @Index(name = "idx_applicant_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Immutable once generated
    @Column(unique = true, nullable = false, updatable = false)
    private String applicationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User applicant;

    @Column(nullable = false)
    private String loanType;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    private Integer declaredCibilScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    // ==========================================
    // 🧠 PROGRESSIVE PROFILING (LEAD SALVAGE ENGINE)
    // ==========================================
    @Column(name = "completion_percentage", nullable = false)
    @Builder.Default
    private Integer completionPercentage = 50; // Starts at 50% post-auth handoff

    @Column(name = "current_step", nullable = false)
    @Builder.Default
    private String currentStep = "COMPLEX_PROFILING";

    // ==========================================
    // RELATIONSHIPS
    // ==========================================
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentRecord> documents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Version
    private Long version;

    // 🧠 UTC Standardized Time (Instant is vastly superior to LocalDateTime for global infra)
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // ==========================================
    // JPA LIFECYCLE HOOKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) status = ApplicationStatus.SUBMITTED;
        if (completionPercentage == null) completionPercentage = 50;
        if (currentStep == null) currentStep = "COMPLEX_PROFILING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ==========================================
    // 🧠 FAILPROOF BI-DIRECTIONAL SYNC
    // These methods prevent Hibernate memory leaks and orphaned rows
    // ALWAYS use these to add/remove documents instead of modifying the list directly
    // ==========================================
    public void addDocument(DocumentRecord document) {
        documents.add(document);
        document.setLoanApplication(this);
    }

    public void removeDocument(DocumentRecord document) {
        documents.remove(document);
        document.setLoanApplication(null);
    }
}