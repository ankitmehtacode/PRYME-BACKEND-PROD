package com.pryme.Backend.crm;

import com.pryme.Backend.document.DocumentRecord;
import com.pryme.Backend.iam.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "loan_applications",
        indexes = {
                // 🧠 INDEXING STRATEGY: Optimized for Admin Dashboards & Lead Retrieval
                @Index(name = "idx_loan_app_status", columnList = "status"),
                @Index(name = "idx_loan_app_applicant", columnList = "user_id"),
                @Index(name = "idx_loan_app_bank", columnList = "selected_bank")
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

    // 🧠 NATURAL ID: The Top 1% practice for JPA equals/hashCode.
    // This cryptographically generated PRYME-ID never changes, making it the perfect business key.
    @NaturalId
    @Column(name = "application_id", unique = true, nullable = false, updatable = false, length = 20)
    private String applicationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "loan_type", nullable = false, length = 50)
    private String loanType;

    // 🧠 THE INTENT ROUTER: Extracted from JSON to a strict column for fast analytics
    @Column(name = "selected_bank", length = 50)
    private String selectedBank;

    @Column(name = "requested_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal requestedAmount = BigDecimal.ZERO;

    @Column(name = "declared_cibil_score")
    private Integer declaredCibilScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    // ==========================================
    // 🧠 PROGRESSIVE PROFILING TRACKERS
    // ==========================================
    @Column(name = "completion_percentage", nullable = false)
    @Builder.Default
    private Integer completionPercentage = 25; // Starts at 25% post-Identity Intake

    @Column(name = "current_step", nullable = false, length = 50)
    @Builder.Default
    private String currentStep = "BANK_SELECTION";

    // 🧠 HYBRID NoSQL COLUMN
    // Maps React's dynamic Funnel Zustand store directly into a highly efficient JSONB column
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    // ==========================================
    // RELATIONSHIPS
    // ==========================================
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentRecord> documents = new ArrayList<>();

    // ==========================================
    // 🧠 CONCURRENCY & TELEMETRY ENGINE
    // ==========================================
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ==========================================
    // JPA LIFECYCLE HOOKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = ApplicationStatus.DRAFT;
        if (this.completionPercentage == null) this.completionPercentage = 25;
        if (this.currentStep == null) this.currentStep = "BANK_SELECTION";
        if (this.metadata == null) this.metadata = new HashMap<>();
        if (this.requestedAmount == null) this.requestedAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ==========================================
    // 🧠 STRICT DOMAIN STATE MACHINE
    // Prevents rogue data transitions (e.g., skipping straight from DRAFT to APPROVED)
    // ==========================================
    public void transitionTo(ApplicationStatus newStatus) {
        if (this.status == ApplicationStatus.DRAFT && newStatus != ApplicationStatus.SUBMITTED && newStatus != ApplicationStatus.PROCESSING) {
            throw new IllegalStateException("Draft applications must transition to SUBMITTED or PROCESSING before underwriting.");
        }
        if (this.status == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot transition a permanently rejected application.");
        }
        this.status = newStatus;
    }

    // ==========================================
    // 🧠 FAILPROOF BI-DIRECTIONAL SYNC
    // These prevent Hibernate memory leaks and orphaned rows in the DB
    // ==========================================
    public void addDocument(DocumentRecord document) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(document);
        document.setLoanApplication(this);
    }

    public void removeDocument(DocumentRecord document) {
        if (this.documents != null) {
            this.documents.remove(document);
            document.setLoanApplication(null);
        }
    }

    // ==========================================
    // 🧠 TOP 1% EQUALS/HASHCODE IMPLEMENTATION
    // NEVER use @Data on a JPA entity. We use the @NaturalId (applicationId)
    // to guarantee absolute identity across transient and persistent states.
    // ==========================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanApplication that = (LoanApplication) o;
        return Objects.equals(applicationId, that.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId);
    }
}