package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.policy.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 📊 Analyzes rule coverage to identify dead or unused policy rules in the active bundle.
 */
@Service
@Slf4j
public class CoverageService {

    public RuleCoverageReport analyzeCoverage(CertificationContext context) {
        PolicyBundle bundle = context.getBundle();
        Map<String, Boolean> matchedRules = context.getMatchedRules();

        List<String> unusedEligibility = new ArrayList<>();
        List<String> unusedFoir = new ArrayList<>();
        List<String> unusedPf = new ArrayList<>();
        List<String> unusedLoginFee = new ArrayList<>();

        int totalRules = 0;
        int matchedCount = 0;

        // 1. Eligibility Rules Coverage
        for (var rule : bundle.eligibilityRules()) {
            String key = makeEligibilityKey(rule);
            totalRules++;
            if (matchedRules.containsKey(key)) {
                matchedCount++;
            } else {
                unusedEligibility.add(key);
            }
        }

        // 2. FOIR Rules Coverage
        for (var rule : bundle.foirRules()) {
            String key = makeFoirKey(rule);
            totalRules++;
            if (matchedRules.containsKey(key)) {
                matchedCount++;
            } else {
                unusedFoir.add(key);
            }
        }

        // 3. Processing Fee Rules Coverage
        for (var rule : bundle.pfRules()) {
            String key = makePfKey(rule);
            totalRules++;
            if (matchedRules.containsKey(key)) {
                matchedCount++;
            } else {
                unusedPf.add(key);
            }
        }

        // 4. Login Fee Rules Coverage
        for (var rule : bundle.loginFeeRules()) {
            String key = makeLoginFeeKey(rule);
            totalRules++;
            if (matchedRules.containsKey(key)) {
                matchedCount++;
            } else {
                unusedLoginFee.add(key);
            }
        }

        double percent = totalRules > 0 ? (double) matchedCount / totalRules * 100.0 : 100.0;

        return new RuleCoverageReport(
                totalRules,
                matchedCount,
                percent,
                unusedEligibility,
                unusedFoir,
                unusedPf,
                unusedLoginFee
        );
    }

    public static String makeEligibilityKey(EligibilityPolicyRule rule) {
        return "ELIGIBILITY::" + rule.lenderName() + "_" + rule.productName() + "_" + rule.surrogate() + "_" + rule.employmentType();
    }

    public static String makeFoirKey(FoirPolicyRule rule) {
        return "FOIR::" + rule.lenderName() + "_" + rule.surrogate() + "_" + rule.employmentType() + "_" + rule.lowerSalary() + "_" + rule.upperSalary();
    }

    public static String makePfKey(ProcessingFeeRule rule) {
        return "PROCESSING_FEE::" + rule.lenderName() + "_" + rule.loanType() + "_" + rule.employmentType() + "_" + rule.minLoanAmount() + "_" + rule.maxLoanAmount();
    }

    public static String makeLoginFeeKey(LoginFeeRule rule) {
        return "LOGIN_FEE::" + rule.lenderName() + "_" + rule.loanType() + "_" + rule.employmentType() + "_" + rule.minLoanAmount() + "_" + rule.maxLoanAmount();
    }

    public record RuleCoverageReport(
        int totalRules,
        int matchedRules,
        double coveragePercentage,
        List<String> unusedEligibilityRules,
        List<String> unusedFoirRules,
        List<String> unusedPfRules,
        List<String> unusedLoginFeeRules
    ) {}
}
