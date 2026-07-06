package com.pryme.Backend.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 🧠 Product ROI Matrix Entity
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Represents a single cell/row in a lender's highly dimensional ROI pricing grid.
 * Replaces the old SpEL dynamic evaluation approach with a secure, easily 
 * queryable relational model.
 * 
 * Dimensions:
 *  - employmentType
 *  - minLoanAmount / maxLoanAmount
 *  - minCibil / maxCibil
 *  - isNtc (New To Credit, usually mapped when CIBIL = 0 or -1)
 *
 * Evaluation Strategy: The FinancialComputationEngine will query these rows
 * and filter them in-memory to find the exact matching ROI for an applicant.
 *
 * @since 2026-05-14 (V18 Migration)
 */
@Entity
@Table(name = "product_roi_matrix")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRoiMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // ── Dimensions (null = "any") ──────────────────────────────────────────

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "min_loan_amount", precision = 15, scale = 2)
    private BigDecimal minLoanAmount;

    @Column(name = "max_loan_amount", precision = 15, scale = 2)
    private BigDecimal maxLoanAmount;

    @Column(name = "min_cibil")
    private Integer minCibil;

    @Column(name = "max_cibil")
    private Integer maxCibil;

    @Column(name = "is_ntc", nullable = false)
    @Builder.Default
    private boolean ntc = false;

    // ── Result ─────────────────────────────────────────────────────────────

    // Stored as decimal: 0.0825 = 8.25% p.a.
    @Column(name = "roi", nullable = false, precision = 6, scale = 4)
    private BigDecimal roi;

    @Column(name = "bundle_id", length = 255)
    private String bundleId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
