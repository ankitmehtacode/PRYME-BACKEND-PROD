package com.pryme.Backend.eligibility.policy.deployment;

import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.loanproduct.entity.ProductPfMatrix;
import com.pryme.Backend.loanproduct.entity.ProductLoginFeeMatrix;
import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import com.pryme.Backend.loanproduct.repository.ProductPfMatrixRepository;
import com.pryme.Backend.loanproduct.repository.ProductLoginFeeMatrixRepository;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyDeploymentService {

    private final EligibilityConditionRepository eligibilityConditionRepository;
    private final ProductPfMatrixRepository pfMatrixRepository;
    private final ProductLoginFeeMatrixRepository loginFeeRepository;
    private final ProductRoiMatrixRepository roiMatrixRepository;
    private final LoanProductRepository loanProductRepository;

    @Transactional
    public void projectBundle(PolicyBundle bundle) {
        String bundleId = bundle.manifest().bundleId();
        log.info("Projecting Policy Bundle {} into database...", bundleId);

        // Project Eligibility Rows
        for (var row : bundle.eligibilityRules()) {
            Long productId = loanProductRepository.findByProductCode(row.productName())
                    .map(p -> p.getId())
                    .orElse(1L);

            BigDecimal parsedLtv = null;
            if (row.ltv() != null) {
                try {
                    parsedLtv = new BigDecimal(row.ltv().trim());
                } catch (Exception e) {
                    // Ignore
                }
            }

            String empType = row.employmentType() != null ? row.employmentType().toUpperCase() : "";
            Integer vintageVal = parseVintage(row.vintage());
            Integer workExp = empType.contains("SALARIED") ? vintageVal : null;
            Integer bizAge = !empType.contains("SALARIED") ? vintageVal : null;

            EligibilityCondition cond = EligibilityCondition.builder()
                    .productId(productId)
                    .productCode(row.productName())
                    .employmentType(row.employmentType())
                    .surrogate(row.surrogate())
                    .minAge(row.minAge())
                    .maxAge(row.maxAge())
                    .minIncome(row.minIncome())
                    .workExpYears(workExp)
                    .businessAgeYears(bizAge)
                    .cibilMin(row.minCibil())
                    .foirMax(null)
                    .minTenure(row.minTenure())
                    .maxTenure(row.maxTenure())
                    .minLoanAmount(row.minLoanAmount())
                    .maxLoanAmount(row.maxLoanAmount())
                    .bankName(row.lenderName())
                    .loanType(row.loanType())
                    .ltvAllowed(parsedLtv)
                    .bundleId(bundleId)
                    .active(true)
                    .build();
            eligibilityConditionRepository.save(cond);
        }

        // Project Processing Fees (PF)
        for (var row : bundle.pfRules()) {
            Long productId = loanProductRepository.findByProductCode(row.productName())
                    .map(p -> p.getId())
                    .orElse(1L);

            ProductPfMatrix pf = ProductPfMatrix.builder()
                    .productId(productId)
                    .employmentType(row.employmentType())
                    .minLoanAmount(row.minLoanAmount())
                    .maxLoanAmount(row.maxLoanAmount())
                    .feeValue(row.pf())
                    .taxRate(row.tax() != null ? row.tax() : new BigDecimal("0.1800"))
                    .bundleId(bundleId)
                    .build();
            pfMatrixRepository.save(pf);
        }

        // Project Login Fees
        for (var row : bundle.loginFeeRules()) {
            Long productId = loanProductRepository.findByProductCode(row.productName())
                    .map(p -> p.getId())
                    .orElse(1L);

            ProductLoginFeeMatrix loginFee = ProductLoginFeeMatrix.builder()
                    .productId(productId)
                    .employmentType(row.employmentType())
                    .minLoanAmount(row.minLoanAmount())
                    .maxLoanAmount(row.maxLoanAmount())
                    .loginFee(row.loginFees())
                    .bundleId(bundleId)
                    .build();
            loginFeeRepository.save(loginFee);
        }

        // Project ROI Slabs
        for (var row : bundle.roiRules()) {
            ProductRoiMatrix roi = ProductRoiMatrix.builder()
                    .productId(row.productId())
                    .employmentType(row.employmentType())
                    .minLoanAmount(row.minLoanAmount())
                    .maxLoanAmount(row.maxLoanAmount())
                    .minCibil(row.minCibil())
                    .maxCibil(row.maxCibil())
                    .ntc(row.ntc())
                    .roi(row.roi())
                    .bundleId(bundleId)
                    .build();
            roiMatrixRepository.save(roi);
        }

        log.info("Database projection for bundle {} completed successfully.", bundleId);
    }

    private Integer parseVintage(String vintage) {
        if (vintage == null || vintage.isBlank()) return null;
        try {
            String digits = vintage.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return null;
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return null;
        }
    }
}
