package com.pryme.Backend.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "loan_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true, length = 20)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    // NIP, Banking, GST, CashFlow, SENP, LAP, HL, BL, PL, CC
    @Column(name = "loan_type", nullable = false, length = 50)
    private String loanType;

    // Plain Long — no JPA join across module boundaries (modular monolith rule)
    @Column(name = "lender_id", nullable = false)
    private Long lenderId;

    @Column(name = "lender_name", nullable = false, length = 100)
    private String lenderName;

    @Column(name = "interest_type", nullable = false, length = 20)
    private String interestType;

    // ── CIBIL ──────────────────────────────────────────────────────────────
    @Column(name = "min_cibil", nullable = false)
    private Integer minCibil;

    @Column(name = "max_cibil", nullable = false)
    private Integer maxCibil;

    // ── Pricing ────────────────────────────────────────────────────────────
    // Stored as decimal: 0.1040 = 10.40% p.a.
    @Column(name = "roi", nullable = false, precision = 6, scale = 4)
    private BigDecimal roi;

    @Column(name = "processing_fee", precision = 6, scale = 4)
    private BigDecimal processingFee;

    @Column(name = "prepayment_charges", precision = 6, scale = 4)
    private BigDecimal prepaymentCharges;

    @Column(name = "foreclosure_charges", precision = 6, scale = 4)
    private BigDecimal foreclosureCharges;

    @Column(name = "login_fees", precision = 12, scale = 2)
    private BigDecimal loginFees;

    @Column(name = "legal_technical_charges", precision = 12, scale = 2)
    private BigDecimal legalTechnicalCharges;

    @Column(name = "other_expense", precision = 12, scale = 2)
    private BigDecimal otherExpense;

    @Column(name = "stamp_duties", precision = 12, scale = 2)
    private BigDecimal stampDuties;

    // ── Tenure (months) ────────────────────────────────────────────────────
    @Column(name = "min_tenure_months", nullable = false)
    private Integer minTenureMonths;

    @Column(name = "max_tenure_months", nullable = false)
    private Integer maxTenureMonths;

    // ── Loan Amount (INR) ──────────────────────────────────────────────────
    @Column(name = "min_loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal minLoanAmount;

    @Column(name = "max_loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxLoanAmount;

    // ── Documentation ──────────────────────────────────────────────────────
    @Column(name = "kyc_requirement")
    private String kycRequirement;

    @Column(name = "income_proof")
    private String incomeProof;

    @Column(name = "bank_statement_months")
    private Integer bankStatementMonths;

    @Column(name = "itr_requirement_years")
    private Integer itrRequirementYears;

    @Column(name = "salary_slip_months")
    private Integer salarySlipMonths;

    @Column(name = "gst_required_months")
    private Integer gstRequiredMonths;

    @Column(name = "residence_profile")
    private String residenceProfile;

    @Column(name = "additional_docs")
    private String additionalDocs;

    // ── Risk & Ratios ──────────────────────────────────────────────────────
    // Product-level FOIR ceiling: 0.95 = 95%.
    // Used as fallback when EligibilityCondition.foirMax is null.
    @Column(name = "max_emi_nmi_ratio", precision = 5, scale = 4)
    private BigDecimal maxEmiNmiRatio;

    // Loan-to-Value ratio: 0.75 = 75%
    @Column(name = "ltv", precision = 5, scale = 4)
    private BigDecimal ltv;

    @Column(name = "obligation_treatment")
    private String obligationTreatment;

    @Column(name = "dpd_allowed")
    private Boolean dpdAllowed;

    @Column(name = "write_off_allowed")
    private Boolean writeOffAllowed;

    @Column(name = "settlement_allowed")
    private Boolean settlementAllowed;

    @Column(name = "risk_category", length = 20)
    private String riskCategory;

    // ── Applicant Profile ──────────────────────────────────────────────────
    @Column(name = "occupation")
    private String occupation;

    @Column(name = "employer_type")
    private String employerType;

    @Column(name = "nature_of_business")
    private String natureOfBusiness;

    @Column(name = "industry")
    private String industry;

    // ── Restrictions ───────────────────────────────────────────────────────
    @Column(name = "pincode_restrictions")
    private String pincodeRestrictions;

    @Column(name = "rejection_codes")
    private String rejectionCodes;

    @Column(name = "auto_reject_conditions")
    private String autoRejectConditions;

    // ── Campaign ───────────────────────────────────────────────────────────
    @Column(name = "campaign_name", length = 100)
    private String campaignName;

    @Column(name = "offer_type", length = 50)
    private String offerType;

    @Column(name = "offer_details")
    private String offerDetails;

    // ── Meta ───────────────────────────────────────────────────────────────
    @Column(name = "notes")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}