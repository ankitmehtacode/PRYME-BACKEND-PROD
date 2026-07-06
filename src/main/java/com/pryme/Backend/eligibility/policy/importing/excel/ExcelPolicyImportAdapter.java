package com.pryme.Backend.eligibility.policy.importing.excel;

import com.pryme.Backend.eligibility.policy.importing.PolicySourceAdapter;
import com.pryme.Backend.eligibility.policy.importing.PolicySourceInput;
import com.pryme.Backend.eligibility.policy.model.*;
import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelPolicyImportAdapter implements PolicySourceAdapter {

    private final ExcelParser parser;
    private final ProductRoiMatrixRepository roiMatrixRepository;

    @Override
    public PolicySourceInput loadSource() {
        try {
            String[] files = {
                "certification/eligibility_workbook.xlsx",
                "certification/FOIR_Sheet.xlsx",
                "certification/PF_data.xlsx",
                "certification/Login_fees.xlsx",
                "certification/HL_LTV_Sheet.xlsx",
                "certification/LAP_LTV_Sheet.xlsx"
            };

            Map<String, String> individualHashes = new HashMap<>();
            MessageDigest combinedDigest = MessageDigest.getInstance("SHA-256");

            for (String file : files) {
                String filename = file.substring(file.lastIndexOf('/') + 1);
                MessageDigest fileDigest = MessageDigest.getInstance("SHA-256");
                try (InputStream is = new ClassPathResource(file).getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fileDigest.update(buffer, 0, bytesRead);
                        combinedDigest.update(buffer, 0, bytesRead);
                    }
                }
                individualHashes.put(filename, toHexString(fileDigest.digest()));
            }

            String combinedHash = toHexString(combinedDigest.digest());

            List<EligibilityPolicyRule> eligibilityRules;
            try (InputStream is = new ClassPathResource("certification/eligibility_workbook.xlsx").getInputStream()) {
                eligibilityRules = parser.parseEligibility(is);
            }

            List<FoirPolicyRule> foirRules;
            try (InputStream is = new ClassPathResource("certification/FOIR_Sheet.xlsx").getInputStream()) {
                foirRules = parser.parseFoir(is);
            }

            List<ProcessingFeeRule> pfRules;
            try (InputStream is = new ClassPathResource("certification/PF_data.xlsx").getInputStream()) {
                pfRules = parser.parsePf(is);
            }

            List<LoginFeeRule> loginFeeRules;
            try (InputStream is = new ClassPathResource("certification/Login_fees.xlsx").getInputStream()) {
                loginFeeRules = parser.parseLoginFee(is);
            }

            List<LowLtvRule> lowLtvRules = new ArrayList<>();
            try (InputStream is = new ClassPathResource("certification/HL_LTV_Sheet.xlsx").getInputStream()) {
                lowLtvRules.addAll(parser.parseHlLtv(is));
            }
            try (InputStream is = new ClassPathResource("certification/LAP_LTV_Sheet.xlsx").getInputStream()) {
                lowLtvRules.addAll(parser.parseLapLtv(is));
            }

            // Map projected DB ROI matrix into domain rules
            List<ProductRoiMatrix> dbRois = roiMatrixRepository.findAll();
            List<ProductRoiMatrixRule> roiRules = dbRois.stream()
                .map(r -> new ProductRoiMatrixRule(
                    r.getProductId(),
                    r.getEmploymentType(),
                    r.getMinLoanAmount(),
                    r.getMaxLoanAmount(),
                    r.getMinCibil(),
                    r.getMaxCibil(),
                    r.isNtc(),
                    r.getRoi()
                ))
                .collect(Collectors.toList());

            return new PolicySourceInput(
                eligibilityRules,
                foirRules,
                pfRules,
                loginFeeRules,
                lowLtvRules,
                roiRules,
                combinedHash,
                individualHashes
            );
        } catch (Exception e) {
            log.error("Failed to load Excel policy sources", e);
            throw new RuntimeException("Error loading Excel policy sources", e);
        }
    }

    private String toHexString(byte[] hashBytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
