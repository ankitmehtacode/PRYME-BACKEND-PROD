package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.policy.engine.PolicyProductMatcher;
import com.pryme.Backend.eligibility.policy.model.*;
import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 🔍 Validates 1-to-1 matching cardinality for all workbook scenarios before replay execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeterminismService {

    private final PolicyProductMatcher policyProductMatcher;
    private final CentralizedNormalizer normalizer;
    private final ProductRoiMatrixRepository roiMatrixRepository;

    public List<DeterminismViolation> validateDeterminism(List<EligibilityPolicyRule> scenarios, CertificationContext context) {
        List<DeterminismViolation> violations = new ArrayList<>();
        List<LoanProduct> products = context.getCatalogSnapshot().products();
        PolicyBundle bundle = context.getBundle();

        for (int i = 0; i < scenarios.size(); i++) {
            EligibilityPolicyRule row = scenarios.get(i);
            int rowNumber = row.excelRowNumber();

            // 1. Verify Product Matching Uniqueness
            EmploymentType empType = normalizer.normalizeToEnum(row.employmentType());
            String codePrefix = normalizer.getProductCodePrefix(row.lenderName(), row.productName());
            System.out.println("[DEBUG-DET] Row " + rowNumber + ": lender=" + row.lenderName() + ", product=" + row.productName() + ", empType=" + empType + ", prefix=" + codePrefix);
            Optional<LoanProduct> matchedProductOpt = Optional.empty();
            try {
                matchedProductOpt = policyProductMatcher.matchOptional(
                        products, row.lenderName(), row.productName(), empType, codePrefix
                );
                System.out.println("[DEBUG-DET] Row " + rowNumber + " match status: " + (matchedProductOpt.isPresent() ? matchedProductOpt.get().getProductCode() : "empty"));
                if (matchedProductOpt.isEmpty()) {
                    violations.add(new DeterminismViolation(rowNumber, RuleDomain.ELIGIBILITY, "PRODUCT", 0, "No active product matches scenario"));
                }
            } catch (Exception e) {
                System.out.println("[DEBUG-DET] Row " + rowNumber + " match exception: " + e.getMessage());
                violations.add(new DeterminismViolation(rowNumber, RuleDomain.ELIGIBILITY, "PRODUCT", 2, e.getMessage()));
            }

            if (matchedProductOpt.isEmpty()) {
                continue; // Cannot validate rules if product doesn't exist
            }
            LoanProduct product = matchedProductOpt.get();

            // 2. Verify Eligibility Rules Cardinality
            long eligibilityMatches = countEligibilityMatches(bundle.eligibilityRules(), row, empType);
            if (eligibilityMatches != 1) {
                violations.add(new DeterminismViolation(rowNumber, RuleDomain.ELIGIBILITY, "ELIGIBILITY_RULE", (int) eligibilityMatches,
                        "Eligibility rule match count was " + eligibilityMatches + " (Expected exactly 1)"));
            }

            BigDecimal loanAmount = row.minLoanAmount() != null ? row.minLoanAmount().max(new BigDecimal("2500000")) : new BigDecimal("2500000");

            // 3. Verify FOIR Rules Cardinality
            String normSurr = normalizer.normalizeSurrogate(row.surrogate());
            if (!"LOW_LTV".equalsIgnoreCase(normSurr)) {
                BigDecimal income = row.minIncome() != null ? row.minIncome().max(new BigDecimal("50000")) : new BigDecimal("50000");
                long foirMatches = countFoirMatches(bundle.foirRules(), row.lenderName(), row.surrogate(), row.employmentType(), income, row.productName());
                if (foirMatches != 1) {
                    violations.add(new DeterminismViolation(rowNumber, RuleDomain.FOIR, "FOIR_RULE", (int) foirMatches,
                            "FOIR rule match count was " + foirMatches + " (Expected exactly 1)"));
                }
            }

            // 4. Verify Processing Fee Rules Cardinality
            long pfMatches = countPfMatches(bundle.pfRules(), row.lenderName(), row.productName(), row.employmentType(), loanAmount);
            if (pfMatches != 1) {
                violations.add(new DeterminismViolation(rowNumber, RuleDomain.PROCESSING_FEE, "PF_RULE", (int) pfMatches,
                        "Processing Fee rule match count was " + pfMatches + " (Expected exactly 1)"));
            }

            // 5. Verify Login Fee Rules Cardinality
            long loginFeeMatches = countLoginFeeMatches(bundle.loginFeeRules(), row.lenderName(), row.productName(), row.employmentType(), loanAmount);
            if (loginFeeMatches != 1) {
                violations.add(new DeterminismViolation(rowNumber, RuleDomain.LOGIN_FEE, "LOGIN_FEE_RULE", (int) loginFeeMatches,
                        "Login Fee rule match count was " + loginFeeMatches + " (Expected exactly 1)"));
            }

            // 6. Verify ROI Rules Cardinality
            boolean hasRoiMatrix = !roiMatrixRepository.findByProductId(product.getId()).isEmpty();
            long roiMatches = countRoiMatches(product.getId(), empType.name(), loanAmount, 750, product.getLenderName());
            long expectedRoi = hasRoiMatrix ? 1 : 0;
            if (roiMatches != expectedRoi) {
                violations.add(new DeterminismViolation(rowNumber, RuleDomain.ROI, "ROI_RULE", (int) roiMatches,
                        "ROI rule match count was " + roiMatches + " (Expected exactly " + expectedRoi + ")"));
            }
        }

        return violations;
    }

    private long countEligibilityMatches(List<EligibilityPolicyRule> rules, EligibilityPolicyRule target, EmploymentType empType) {
        String normLender = normalizer.normalizeLender(target.lenderName());
        String normProduct = target.productName() != null ? target.productName().trim() : "HL";
        String normSurrogate = normalizer.normalizeSurrogate(target.surrogate());
        String normSep = target.selfEmployedProfessional() != null ? target.selfEmployedProfessional().trim() : "";
        String normMargin = target.marginByOccupation() != null ? target.marginByOccupation().trim() : "";
        String normProp = target.propertyType() != null ? target.propertyType().trim() : "";

        return rules.stream()
                .filter(r -> normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender))
                .filter(r -> r.productName() != null && r.productName().trim().equalsIgnoreCase(normProduct))
                .filter(r -> normalizer.normalizeSurrogate(r.surrogate()).equalsIgnoreCase(normSurrogate))
                .filter(r -> normalizer.normalizeToEnum(r.employmentType()) == empType)
                .filter(r -> {
                    String rSep = r.selfEmployedProfessional() != null ? r.selfEmployedProfessional().trim() : "";
                    return rSep.equalsIgnoreCase(normSep);
                })
                .filter(r -> {
                    String rMargin = r.marginByOccupation() != null ? r.marginByOccupation().trim() : "";
                    return rMargin.equalsIgnoreCase(normMargin);
                })
                .filter(r -> {
                    String rProp = r.propertyType() != null ? r.propertyType().trim() : "";
                    return rProp.equalsIgnoreCase(normProp);
                })
                .count();
    }

    private long countFoirMatches(List<FoirPolicyRule> rules, String lender, String surrogate, String empType, BigDecimal income, String loanType) {
        String normLender = normalizer.normalizeLender(lender);
        String normSurrogate = normalizer.normalizeSurrogate(surrogate);

        long count = rules.stream()
                .filter(r -> normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender))
                .filter(r -> r.productName() != null && r.productName().trim().equalsIgnoreCase(loanType))
                .filter(r -> normalizer.normalizeSurrogate(r.surrogate()).equalsIgnoreCase(normSurrogate))
                .filter(r -> normalizer.matchRoiEmploymentType(r.employmentType(), empType, lender))
                .filter(r -> {
                    BigDecimal minVal = r.lowerSalary() != null ? r.lowerSalary() : BigDecimal.ZERO;
                    BigDecimal maxVal = r.upperSalary() != null ? r.upperSalary() : new BigDecimal("999999999");
                    return income.compareTo(minVal) >= 0 && income.compareTo(maxVal) <= 0;
                })
                .count();

        if (count == 0 && ("YES BANK".equalsIgnoreCase(normLender) || "ICICI Bank".equalsIgnoreCase(normLender) || "Aditya Birla Finance Limited".equalsIgnoreCase(normLender) || "JIO Finance".equalsIgnoreCase(normLender))) {
            log.info("[DIAGNOSTIC] countFoirMatches failed for lender={}, surrogate={}, empType={}, income={}, loanType={}",
                    normLender, normSurrogate, empType, income, loanType);
            for (FoirPolicyRule r : rules) {
                if (normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender)) {
                    BigDecimal minVal = r.lowerSalary() != null ? r.lowerSalary() : BigDecimal.ZERO;
                    BigDecimal maxVal = r.upperSalary() != null ? r.upperSalary() : new BigDecimal("999999999");
                    log.info("  Rule: prod={}, surr={}, emp={}, lowerSalary={}, upperSalary={}, foir={}, matchProd={}, matchSurr={}, matchEmp={}, matchIncome={}",
                            r.productName(), r.surrogate(), r.employmentType(), r.lowerSalary(), r.upperSalary(), r.foir(),
                            (r.productName() != null && r.productName().trim().equalsIgnoreCase(loanType)),
                            normalizer.normalizeSurrogate(r.surrogate()).equalsIgnoreCase(normSurrogate),
                            normalizer.matchRoiEmploymentType(r.employmentType(), empType, lender),
                            (income.compareTo(minVal) >= 0 && income.compareTo(maxVal) <= 0)
                    );
                }
            }
        }
        return count;
    }

    private long countPfMatches(List<ProcessingFeeRule> rules, String lender, String loanType, String empType, BigDecimal loanAmount) {
        String normLender = normalizer.normalizeLender(lender);

        return rules.stream()
                .filter(r -> normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender))
                .filter(r -> r.productName() != null && r.productName().trim().equalsIgnoreCase(loanType))
                .filter(r -> normalizer.matchRoiEmploymentType(r.employmentType(), empType, lender))
                .filter(r -> {
                    BigDecimal minAmt = r.minLoanAmount() != null ? r.minLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = r.maxLoanAmount() != null ? r.maxLoanAmount() : new BigDecimal("999999999");
                    return loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0;
                })
                .count();
    }

    private long countLoginFeeMatches(List<LoginFeeRule> rules, String lender, String loanType, String empType, BigDecimal loanAmount) {
        String normLender = normalizer.normalizeLender(lender);

        return rules.stream()
                .filter(r -> normalizer.normalizeLender(r.lenderName()).equalsIgnoreCase(normLender))
                .filter(r -> r.productName() != null && r.productName().trim().equalsIgnoreCase(loanType))
                .filter(r -> normalizer.matchRoiEmploymentType(r.employmentType(), empType, lender))
                .filter(r -> {
                    BigDecimal minAmt = r.minLoanAmount() != null ? r.minLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = r.maxLoanAmount() != null ? r.maxLoanAmount() : new BigDecimal("999999999");
                    return loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0;
                })
                .count();
    }

    private long countRoiMatches(Long productId, String empType, BigDecimal loanAmount, int cibil, String lenderName) {
        if (productId == null) return 0;
        List<ProductRoiMatrix> matrixRows = roiMatrixRepository.findByProductId(productId);
        if (matrixRows == null) return 0;


        return matrixRows.stream()
                .filter(row -> !row.isNtc())
                .filter(row -> {
                    String rowEmpType = row.getEmploymentType();
                    if (rowEmpType != null) {
                        return normalizer.matchRoiEmploymentType(rowEmpType, empType, lenderName);
                    }
                    return true;
                })
                .filter(row -> {
                    BigDecimal minAmt = row.getMinLoanAmount() != null ? row.getMinLoanAmount() : BigDecimal.ZERO;
                    BigDecimal maxAmt = row.getMaxLoanAmount() != null ? row.getMaxLoanAmount() : new BigDecimal("999999999");
                    return loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0;
                })
                .filter(row -> {
                    Integer minCibil = row.getMinCibil();
                    Integer maxCibil = row.getMaxCibil();
                    return (minCibil == null || cibil >= minCibil) && (maxCibil == null || cibil <= maxCibil);
                })
                .count();
    }

    public record DeterminismViolation(
        int rowNumber,
        RuleDomain domain,
        String key,
        int actualMatches,
        String details
    ) {}
}
