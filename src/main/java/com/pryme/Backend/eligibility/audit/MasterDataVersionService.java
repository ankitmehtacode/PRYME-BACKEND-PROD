package com.pryme.Backend.eligibility.audit;

import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterDataVersionService {

    private final EligibilityConditionRepository eligibilityConditionRepository;

    @Cacheable("masterDataVersion")
    public String computeVersion() {
        List<EligibilityCondition> conditions = eligibilityConditionRepository.findByActive(true);
        if (conditions == null || conditions.isEmpty()) {
            return "sha256:empty";
        }

        // Sort by ID for absolute determinism (create a mutable copy first)
        List<EligibilityCondition> sortedConditions = new ArrayList<>(conditions);
        sortedConditions.sort(Comparator.comparing(EligibilityCondition::getId));

        StringBuilder sb = new StringBuilder();
        for (EligibilityCondition c : sortedConditions) {
            sb.append(c.getId()).append("|")
              .append(c.getProductCode() != null ? c.getProductCode() : "").append("|")
              .append(c.getEmploymentType() != null ? c.getEmploymentType() : "").append("|")
              .append(c.getSurrogate() != null ? c.getSurrogate() : "").append("|")
              .append(c.getMinAge() != null ? c.getMinAge() : "").append("|")
              .append(c.getMaxAge() != null ? c.getMaxAge() : "").append("|")
              .append(c.getMinIncome() != null ? c.getMinIncome() : "").append("|")
              .append(c.getCibilMin() != null ? c.getCibilMin() : "").append("|")
              .append(c.getFoirMax() != null ? c.getFoirMax() : "").append("|")
              .append(c.getMinTenure() != null ? c.getMinTenure() : "").append("|")
              .append(c.getMaxTenure() != null ? c.getMaxTenure() : "").append("|")
              .append(c.getMinLoanAmount() != null ? c.getMinLoanAmount() : "").append("|")
              .append(c.getMaxLoanAmount() != null ? c.getMaxLoanAmount() : "").append("|")
              .append(c.getLtvAllowed() != null ? c.getLtvAllowed() : "").append("|")
              .append(c.getDeviationFormulae() != null ? c.getDeviationFormulae() : "").append("|")
              .append(c.getConditions() != null ? c.getConditions() : "").append("|")
              .append(c.getPropertyType() != null ? c.getPropertyType() : "").append("|")
              .append(c.getNegativeProperty() != null ? c.getNegativeProperty() : "").append("|")
              .append(c.getProfileRestrictions() != null ? c.getProfileRestrictions() : "").append("\n");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder("sha256:");
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String version = hexString.toString();
            log.info("Generated master data version hash: {}", version);
            return version;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return "sha256:unknown";
        }
    }
}
