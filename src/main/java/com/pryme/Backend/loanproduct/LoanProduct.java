package com.pryme.Backend.loanproduct;

import com.pryme.Backend.bankconfig.Bank;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "loan_products", indexes = {
        @Index(name = "idx_loan_products_min_salary", columnList = "min_salary"),
        @Index(name = "idx_loan_products_min_cibil", columnList = "min_cibil")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(name = "min_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "min_cibil", nullable = false)
    private Integer minCibil;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "processing_fee", nullable = false, precision = 6, scale = 2)
    private BigDecimal processingFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanProductType type;
}
