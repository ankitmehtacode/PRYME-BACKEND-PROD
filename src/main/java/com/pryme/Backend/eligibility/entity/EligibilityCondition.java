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

    // NIP (Net Income Program), BANKING (ABB), GST, STANDARD
    @Column(name = "surrogate", length = 50)
    private String surrogate;

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

    @Column(name = "foir_max", precision = 5, scale = 4)
    private BigDecimal foirMax;

    @Column(name = "min_tenure")
    private Integer minTenure;

    @Column(name = "max_tenure")
    private Integer maxTenure;

    @Column(name = "min_loan_amount", precision = 15, scale = 2)
    private BigDecimal minLoanAmount;

    @Column(name = "max_loan_amount", precision = 15, scale = 2)
    private BigDecimal maxLoanAmount;



    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "loan_type")
    private String loanType;

    @Column(name = "itr_required_years")
    private Integer itrRequiredYears;

    @Column(name = "ltv_allowed", precision = 5, scale = 4)
    private BigDecimal ltvAllowed;



    @Column(name = "deviation_formulae", columnDefinition = "text")
    private String deviationFormulae;

    @Column(name = "conditions", columnDefinition = "text")
    private String conditions;

    @Column(name = "emi_not_obligated")
    private String emiNotObligated;

    @Column(name = "property_type", columnDefinition = "text")
    private String propertyType;

    @Column(name = "vintage", length = 50)
    private String vintage;

    @Column(name = "bank_statement_requirement")
    private String bankStatementRequirement;

    @Column(name = "salary_slip_requirement")
    private String salarySlipRequirement;

    @Column(name = "gst_return_requirement")
    private String gstReturnRequirement;

    @Column(name = "negative_property", columnDefinition = "text")
    private String negativeProperty;

    @Column(name = "negative_employer_type", columnDefinition = "text")
    private String negativeEmployerType;

    @Column(name = "negative_salary_mode", columnDefinition = "text")
    private String negativeSalaryMode;

    @Column(name = "margin_by_occupation", columnDefinition = "text")
    private String marginByOccupation;

    @Column(name = "provident_fund_mandatory")
    private Boolean providentFundMandatory;

    @Column(name = "city_tier", length = 20)
    private String cityTier;

    @Column(name = "profile_restrictions", columnDefinition = "text")
    private String profileRestrictions;

    @Column(name = "notes", columnDefinition = "text")
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
