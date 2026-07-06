package com.pryme.Backend.eligibility.policy.api;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class PolicyBundleDetailsResponse {
    private String bundleId;
    private String version;
    private String combinedHash;
    private String workbookHash;
    private String state;
    private String createdBy;
    private Instant createdAt;
    private String certificationId;
    private boolean active;
}
