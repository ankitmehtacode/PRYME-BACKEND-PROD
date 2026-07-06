package com.pryme.Backend.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 🧠 Product Login Fee Matrix Entity
 *
 * Represents a single row in the dynamic login fee slab setup.
 * Maps exact login fees by loan amount and employment type.
 */
@Entity
@Table(name = "product_login_fee_matrix")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductLoginFeeMatrix {

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

    @Column(name = "login_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal loginFee;

    @Column(name = "bundle_id", length = 255)
    private String bundleId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
