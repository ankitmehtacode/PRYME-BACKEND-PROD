package com.pryme.Backend.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 Product FOIR Matrix Entity
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Represents a specific FOIR mapping lane for a loan product.
 * Replaces the old single-column foir_max with a matrix-driven approach supporting
 * dynamic resolution by employment type, surrogate, and salary slabs.
 */
@Entity
@Table(name = "product_foir_matrix")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFoirMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "surrogate")
    private String surrogate;

    @Column(name = "min_salary", precision = 15, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 15, scale = 2)
    private BigDecimal maxSalary;

    @Column(name = "foir", nullable = false, precision = 6, scale = 4)
    private BigDecimal foir;

    @Column(name = "deviation", precision = 6, scale = 4)
    private BigDecimal deviation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
