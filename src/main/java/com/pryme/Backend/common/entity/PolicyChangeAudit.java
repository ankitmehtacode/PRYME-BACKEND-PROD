package com.pryme.Backend.common.entity;

import lombok.*;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedDate;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_change_audits")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class PolicyChangeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String fieldKey;

    private String oldValue;

    @Column(nullable = false)
    private String newValue;

    @Column(nullable = false)
    private Long changedByUserId;

    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime effectiveFrom;
}
