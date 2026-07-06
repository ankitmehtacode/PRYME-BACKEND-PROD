package com.pryme.Backend.eligibility.policy.projection;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "policy_bundle")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyBundleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bundle_id", nullable = false, unique = true)
    private String bundleId;

    @Column(name = "version")
    private String version;

    @Column(name = "combined_hash")
    private String combinedHash;

    @Column(name = "policy_bundle_hash")
    private String policyBundleHash;

    @Column(name = "state")
    private String state;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "certification_id")
    private String certificationId;

    @Column(name = "active")
    private boolean active;
}
