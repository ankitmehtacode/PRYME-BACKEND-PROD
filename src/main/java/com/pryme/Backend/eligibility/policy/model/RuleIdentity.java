package com.pryme.Backend.eligibility.policy.model;

/**
 * 🆔 Immutable stable identity for a policy rule.
 */
public record RuleIdentity(
    String ruleId,
    RuleDomain domain,
    int priority
) {}
