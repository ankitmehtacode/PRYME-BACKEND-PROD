package com.pryme.Backend.iam;

import java.time.Instant;
import java.util.UUID;

/**
 * 🧠 ZERO-TRUST SESSION PROJECTION DTO
 * 
 * Masks raw IP address (last octet hidden) and strips UserAgent.
 * Prevents IP enumeration and browser fingerprint leakage.
 * The raw SessionRecord entity must NEVER cross the API boundary.
 */
public record SessionResponse(
    UUID id,
    String maskedIp,
    String deviceType,
    boolean active,
    Instant createdAt,
    Instant expiresAt
) {
    /**
     * Factory method: SessionRecord → Safe DTO
     */
    public static SessionResponse from(SessionRecord session) {
        return new SessionResponse(
            session.getId(),
            maskIp(session.getIpAddress()),
            parseDeviceType(session.getUserAgent()),
            session.getIsActive() != null && session.getIsActive(),
            session.getCreatedAt(),
            session.getExpiresAt()
        );
    }

    /**
     * Masks the last octet of an IPv4 address (e.g., "192.168.1.42" → "192.168.1.***")
     * For IPv6, truncates after the 4th group.
     */
    private static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return "Unknown";
        
        // IPv4
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".***";
        }
        
        // IPv6: show first 4 groups only
        String[] groups = ip.split(":");
        if (groups.length > 4) {
            return String.join(":", groups[0], groups[1], groups[2], groups[3]) + ":****";
        }
        
        return "***";
    }

    /**
     * Extracts a human-friendly device type from the UserAgent string.
     */
    private static String parseDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown Device";

        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "Mobile";
        if (ua.contains("tablet") || ua.contains("ipad")) return "Tablet";
        if (ua.contains("postman")) return "API Client";
        if (ua.contains("swagger") || ua.contains("insomnia")) return "API Client";
        return "Desktop";
    }
}
