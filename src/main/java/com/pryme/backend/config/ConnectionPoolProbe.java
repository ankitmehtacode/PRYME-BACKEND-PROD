package com.pryme.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ConnectionPoolProbe {
    private final HikariDataSource dataSource;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger saturatedCycles = new AtomicInteger(0);
    private final AtomicBoolean saturated = new AtomicBoolean(false);
    private volatile double currentUtilisation = 0.0;

    public ConnectionPoolProbe(HikariDataSource dataSource, MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
        Gauge.builder("hikari.pool.utilisation", this, ConnectionPoolProbe::getCurrentUtilisation)
                .description("HikariCP connection pool utilisation ratio")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    public void probe() {
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        int active = pool.getActiveConnections();
        int total = pool.getTotalConnections();
        if (total == 0) return;
        currentUtilisation = (double) active / total;

        if (currentUtilisation >= 0.95) {
            if (saturatedCycles.incrementAndGet() >= 2) {
                saturated.set(true);
            }
        } else if (currentUtilisation < 0.80) {
            saturatedCycles.set(0);
            saturated.set(false);
        }
    }

    public boolean isSaturated() { return saturated.get(); }

    public double getCurrentUtilisation() { return currentUtilisation; }
}
