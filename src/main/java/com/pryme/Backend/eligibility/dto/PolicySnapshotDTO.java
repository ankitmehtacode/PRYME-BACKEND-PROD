package com.pryme.Backend.eligibility.dto;

import java.math.BigDecimal;

public record PolicySnapshotDTO(
    // ── EligibilityCondition fields (ALL 30) ──
    Long ruleId,
    Long productId,
    String productCode,
    String employmentType,
    String surrogate,
    Integer minAge, Integer maxAge,
    BigDecimal minIncome,
    String incomeType,
    Integer workExpYears, Integer businessAgeYears,
    Integer cibilMin,
    BigDecimal foirMax, BigDecimal ltvAllowed,
    String bankName, String loanType,
    Integer itrRequiredYears,
    String deviationFormulae, String conditions,
    Boolean emiNotObligated,
    String propertyType, String negativeProperty,
    String negativeEmployerType, String negativeSalaryMode,
    String marginByOccupation,
    Boolean providentFundMandatory,
    String cityTier,
    String profileRestrictions,
    String notes,
    boolean active,
    
    // ── Linked LoanProduct fields (resolved by productCode) ──
    String productName,
    String lenderName,
    String interestType,
    BigDecimal roi, BigDecimal processingFee,
    BigDecimal prepaymentCharges, BigDecimal foreclosureCharges,
    BigDecimal loginFees, BigDecimal legalTechnicalCharges,
    BigDecimal otherExpense, String insuranceCharges, BigDecimal stampDuties,
    Integer minTenureMonths, Integer maxTenureMonths,
    BigDecimal minLoanAmount, BigDecimal maxLoanAmount,
    Integer minCibil, Integer maxCibil,
    String kycRequirement, String incomeProof,
    Integer bankStatementMonths, Integer itrRequirementYears,
    Integer salarySlipMonths, Integer gstRequiredMonths,
    String residenceProfile, String additionalDocs,
    BigDecimal maxEmiNmiRatio, BigDecimal ltv,
    String obligationTreatment,
    Boolean dpdAllowed, Boolean writeOffAllowed, Boolean settlementAllowed,
    String riskCategory,
    String occupation, String employerType,
    String natureOfBusiness, String industry,
    String pincodeRestrictions,
    String campaignName, String offerType, String offerDetails
) {}
