package com.pryme.Backend.eligibility.policy.engine;

import java.util.ArrayList;
import java.util.List;

public class PolicyExecutionContext {
    private final List<String> matchedRules = new ArrayList<>();
    private final List<String> failedRules = new ArrayList<>();
    private int ruleHits = 0;
    private int ruleMisses = 0;
    private long latencyNs = 0;
    private int formulaCount = 0;

    public void recordHit(String ruleId) {
        matchedRules.add(ruleId);
        ruleHits++;
    }

    public void recordMiss(String ruleId) {
        failedRules.add(ruleId);
        ruleMisses++;
    }

    public void incrementFormulaCount() {
        formulaCount++;
    }

    public void setLatencyNs(long latencyNs) {
        this.latencyNs = latencyNs;
    }

    public List<String> getMatchedRules() {
        return matchedRules;
    }

    public List<String> getFailedRules() {
        return failedRules;
    }

    public int getRuleHits() {
        return ruleHits;
    }

    public int getRuleMisses() {
        return ruleMisses;
    }

    public long getLatencyNs() {
        return latencyNs;
    }

    public int getFormulaCount() {
        return formulaCount;
    }
}
