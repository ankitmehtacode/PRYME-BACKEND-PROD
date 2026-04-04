package com.pryme.backend.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ConnectionPoolHealthIndicator implements HealthIndicator {
    private final ConnectionPoolProbe probe;

    public ConnectionPoolHealthIndicator(ConnectionPoolProbe probe) {
        this.probe = probe;
    }

    @Override
    public Health health() {
        if (probe.isSaturated()) {
            return Health.outOfService()
                    .withDetail("reason", "Connection pool saturated for >10s")
                    .withDetail("utilisation", String.format("%.1f%%", probe.getCurrentUtilisation() * 100))
                    .build();
        }
        return Health.up()
                .withDetail("utilisation", String.format("%.1f%%", probe.getCurrentUtilisation() * 100))
                .build();
    }
}
