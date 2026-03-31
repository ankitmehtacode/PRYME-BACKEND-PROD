// File: src/main/java/com/pryme/Backend/eligibility/entity/EligibilityCondition.java

package com.pryme.Backend.eligibility.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "eligibility_conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "min_income", precision = 12, scale = 2)
    private BigDecimal minIncome;

    @Column(name = "income_type", length = 50)
    private String incomeType;

    @Column(name = "work_exp_years")
    private Integer workExpYears;

    @Column(name = "business_age_years")
    private Integer businessAgeYears;

    @Column(name = "cibil_min")
    private Integer cibilMin;

    // Stored as decimal: 0.95 = 95%, 0.55 = 55%
    @Column(name = "foir_max", precision = 5, scale = 4)
    private BigDecimal foirMax;

    @Column(name = "property_type")
    private String propertyType;

    @Column(name = "city_tier", length = 20)
    private String cityTier;

    @Column(name = "profile_restrictions")
    private String profileRestrictions;

    @Column(name = "notes")
    private String notes;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
