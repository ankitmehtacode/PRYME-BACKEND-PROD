package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.loanproduct.dto.ProductCatalogSnapshot;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🌐 Read-only snapshot context shared across certification execution stages.
 */
@Getter
@Builder
public class CertificationContext {
    private final PolicyBundle bundle;
    private final ProductCatalogSnapshot catalogSnapshot;
    private final Instant startTime;

    // Thread-safe results and trackers
    private final List<CertificationReportModels.FieldMismatch> allDeviations = java.util.Collections.synchronizedList(new ArrayList<>());
    private final List<CertificationReportModels.PipelineAuditItem> pipelineItems = java.util.Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, List<CertificationReportModels.FieldMismatch>> rowDeviations = new ConcurrentHashMap<>();
    private final Map<String, Boolean> matchedRules = new ConcurrentHashMap<>();

    public void addDeviation(int rowIdx, CertificationReportModels.FieldMismatch deviation) {
        allDeviations.add(deviation);
        rowDeviations.computeIfAbsent(rowIdx, k -> java.util.Collections.synchronizedList(new ArrayList<>())).add(deviation);
    }

    public void addPipelineItem(CertificationReportModels.PipelineAuditItem item) {
        pipelineItems.add(item);
    }

    public void registerRuleMatch(String domain, String ruleId) {
        if (ruleId != null) {
            matchedRules.put(domain + "::" + ruleId, true);
        }
    }
}
