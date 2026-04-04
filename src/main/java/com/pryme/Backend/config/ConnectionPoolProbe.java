package com.pryme.Backend.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ConnectionPoolProbe {

    private final HikariDataSource dataSource;
    private final AtomicInteger highUtilisationCycles = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private volatile double utilisation = 0.0;
    final AtomicBoolean saturated = new AtomicBoolean(false);

    public ConnectionPoolProbe(HikariDataSource dataSource, MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        Gauge.builder("hikari.pool.utilisation", () -> utilisation).register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    public void probe() {
        var poolMxBean = dataSource.getHikariPoolMXBean();
        int active = poolMxBean.getActiveConnections();
        int max = poolMxBean.getTotalConnections();

        this.activeConnections.set(active);
        this.utilisation = max == 0 ? 0.0 : (double) active / max;

        if (utilisation >= 0.95) {
            if (highUtilisationCycles.incrementAndGet() >= 2) {
                saturated.set(true);
            }
            return;
        }

        if (utilisation < 0.8) {
            saturated.set(false);
            highUtilisationCycles.set(0);
        }
    }

    public boolean isSaturated() {
        return saturated.get();
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }
}
