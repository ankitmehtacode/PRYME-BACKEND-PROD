package com.pryme.Backend.common.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_field_definitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyFieldDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fieldKey;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyEntityType entityType;

    private BigDecimal absoluteLowerBound;
    private BigDecimal absoluteUpperBound;

    private String allowedValues;
    private String unit;

    @Column(nullable = false)
    private boolean requiresReason;

    @Column(nullable = false)
    private boolean isActive;

    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum FieldType {
        NUMERIC_RANGE, PERCENTAGE, INTEGER, BOOLEAN, TEXT, ENUM_LIST
    }

    public enum PolicyEntityType {
        LOAN_PRODUCT, ELIGIBILITY_CONDITION, GENERAL_BANK_POLICY, SURROGATE_POLICY
    }
}
