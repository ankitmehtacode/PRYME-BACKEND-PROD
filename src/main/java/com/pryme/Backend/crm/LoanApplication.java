package com.pryme.Backend.crm;

import com.pryme.Backend.iam.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String applicationId; // e.g., PRY-9021

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User applicant; // Link to Authenticated User

    private String loanType; // LAP, PL, HL
    private BigDecimal requestedAmount;
    private Integer declaredCibilScore;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    // 🧠 NEW: CRM Assignment Tracking
    @Builder.Default
    @Column(name = "assignee_id")
    private String assignee = "UNASSIGNED";

    @Version
    private Long version; // Optimistic locking for concurrent Employee edits

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ApplicationStatus.SUBMITTED;
        if (assignee == null) assignee = "UNASSIGNED"; // Failsafe for direct entity instantiations
    }
}