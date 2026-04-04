package com.pryme.Backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnectionPoolHealthIndicator implements HealthIndicator {

    private final ConnectionPoolProbe probe;

    @Override
    public Health health() {
        if (probe.isSaturated()) {
            return Health.outOfService()
                    .withDetail("reason", "Connection pool at capacity")
                    .build();
        }

        return Health.up()
                .withDetail("activeConnections", probe.getActiveConnections())
                .build();
    }
}
