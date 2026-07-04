package com.pryme.Backend.eligibility.audit.certification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelPolicyProvider implements PolicyProvider {

    private final ExcelWorkbookParser parser;

    @Override
    public PolicyBundle load() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Define all files to load
            String[] files = {
                "certification/eligibility_workbook.xlsx",
                "certification/FOIR_Sheet.xlsx",
                "certification/PF_data.xlsx",
                "certification/Login_fees.xlsx",
                "certification/HL_LTV_Sheet.xlsx",
                "certification/LAP_LTV_Sheet.xlsx"
            };

            // Calculate overall hash
            for (String file : files) {
                try (InputStream is = new ClassPathResource(file).getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        digest.update(buffer, 0, bytesRead);
                    }
                }
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder("sha256:");
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String combinedHash = hexString.toString();

            // Parse each sheet
            List<WorkbookModels.EligibilityRow> eligibilityRows;
            try (InputStream is = new ClassPathResource("certification/eligibility_workbook.xlsx").getInputStream()) {
                eligibilityRows = parser.parseEligibilityWorkbook(is);
            }

            List<WorkbookModels.FoirRow> foirRows;
            try (InputStream is = new ClassPathResource("certification/FOIR_Sheet.xlsx").getInputStream()) {
                foirRows = parser.parseFoirWorkbook(is);
            }

            List<WorkbookModels.PfRow> pfRows;
            try (InputStream is = new ClassPathResource("certification/PF_data.xlsx").getInputStream()) {
                pfRows = parser.parsePfWorkbook(is);
            }

            List<WorkbookModels.LoginFeeRow> loginFeeRows;
            try (InputStream is = new ClassPathResource("certification/Login_fees.xlsx").getInputStream()) {
                loginFeeRows = parser.parseLoginFeeWorkbook(is);
            }

            List<WorkbookModels.HlLtvRow> hlLtvRows;
            try (InputStream is = new ClassPathResource("certification/HL_LTV_Sheet.xlsx").getInputStream()) {
                hlLtvRows = parser.parseHlLtvWorkbook(is);
            }

            List<WorkbookModels.LapLtvRow> lapLtvRows;
            try (InputStream is = new ClassPathResource("certification/LAP_LTV_Sheet.xlsx").getInputStream()) {
                lapLtvRows = parser.parseLapLtvWorkbook(is);
            }

            return new PolicyBundle(
                eligibilityRows,
                foirRows,
                pfRows,
                loginFeeRows,
                hlLtvRows,
                lapLtvRows,
                combinedHash
            );
        } catch (Exception e) {
            log.error("Failed to load policy bundle from Excel", e);
            throw new RuntimeException("Error loading policy bundle", e);
        }
    }
}
