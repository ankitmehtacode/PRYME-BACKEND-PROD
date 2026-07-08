package com.pryme.Backend.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 🧠 Product PF Matrix Entity
 *
 * Represents a single row in the dynamic processing fee slab setup.
 * Handles both flat fees and percentage-based calculations with optional min/max boundaries and tax rates.
 */
@Entity
@Table(name = "product_pf_matrix")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPfMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "employment_type", nullable = false, length = 50)
    private String employmentType;

    @Column(name = "min_loan_amount", precision = 15, scale = 2)
    private BigDecimal minLoanAmount;

    @Column(name = "max_loan_amount", precision = 15, scale = 2)
    private BigDecimal maxLoanAmount;

    @Column(name = "fee_value", nullable = false, precision = 14, scale = 4)
    private BigDecimal feeValue;

    @Column(name = "is_flat", nullable = false)
    @Builder.Default
    private boolean flat = false;

    @Column(name = "min_fee", precision = 15, scale = 2)
    private BigDecimal minFee;

    @Column(name = "max_fee", precision = 15, scale = 2)
    private BigDecimal maxFee;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal taxRate = new BigDecimal("0.1800");

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
