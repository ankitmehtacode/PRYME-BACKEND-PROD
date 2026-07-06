package com.pryme.Backend.eligibility.policy.projection;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "policy_activation_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyActivationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activation_id", nullable = false, unique = true)
    private String activationId;

    @Column(name = "bundle_id")
    private String bundleId;

    @Column(name = "bundle_hash")
    private String bundleHash;

    @Column(name = "policy_version")
    private String policyVersion;

    @Column(name = "state")
    private String state;

    @Column(name = "activated_by")
    private String activatedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "git_commit")
    private String gitCommit;

    @Column(name = "certification_id")
    private String certificationId;

    @Column(name = "policy_bundle_hash")
    private String policyBundleHash;

    @Column(name = "rollback_bundle")
    private String rollbackBundle;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at")
    private Instant createdAt;
}
