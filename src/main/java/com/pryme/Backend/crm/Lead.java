package com.pryme.Backend.crm;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads", indexes = {
        @Index(name = "idx_leads_status", columnList = "status"),
        @Index(name = "idx_leads_created_at", columnList = "createdAt"),
        @Index(name = "idx_leads_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String userName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(nullable = false, length = 30)
    private String loanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Column(length = 50)
    private String offerId;

    @Column(length = 40, unique = true)
    private String idempotencyKey;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = LeadStatus.NEW;
        }
    }
}
