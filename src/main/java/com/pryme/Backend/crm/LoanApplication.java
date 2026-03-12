package com.pryme.Backend.crm;

import com.pryme.Backend.document.DocumentRecord;
import com.pryme.Backend.iam.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User applicant;

    private String loanType;
    private BigDecimal requestedAmount;
    private Integer declaredCibilScore;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentRecord> documents = new ArrayList<>();

    // 🧠 Production Grade Foreign Key mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Version
    private Long version;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ApplicationStatus.SUBMITTED;
    }
}