package com.pryme.Backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class LifecycleConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(LifecycleConfig.class);

    // ==========================================
    // 🧠 1. THE ELASTIC ASYNC ENGINE (High Traffic Absorber)
    // Used by @Async methods (like the EligibilityEvent Listener)
    // ==========================================
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // The Elastic Boundaries
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(500); // Absorbs massive concurrency spikes

        // 4000 User Buffer: Queues requests in RAM instead of crashing with OutOfMemoryError
        executor.setQueueCapacity(4000);
        executor.setThreadNamePrefix("PrymeAsync-");

        // 🧠 RESOURCE OPTIMIZATION: The Thread Terminator
        // Forces JVM to kill all unused threads (even core) after 60 seconds of idle time.
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);

        // Backpressure Protocol: If the 4000 queue is full, the calling thread processes it itself.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Graceful Shutdown: Ensures active loan processing finishes before Tomcat restarts
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        log.info("Titanium Async Engine Initialized. Elastic Bounds: 0 to 500 threads.");
        return executor;
    }

    // ==========================================
    // 🧠 2. THE BACKGROUND SCHEDULER (Vacuum & Maintenance)
    // Used by @Scheduled methods (like the Session Sweeper)
    // ==========================================
    @Bean
    public TaskScheduler taskScheduler(
            @Value("${app.runtime.scheduler.pool-size:2}") int poolSize,
            @Value("${app.runtime.scheduler.await-termination-seconds:30}") int awaitTerminationSeconds
    ) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, poolSize));
        scheduler.setThreadNamePrefix("PrymeCron-");

        // Guarantees cron jobs finish cleanly before the server shuts down
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(Math.max(5, awaitTerminationSeconds));

        // 🧠 SILICON-VALLEY ERROR CONTAINMENT
        // Instead of an empty lambda, we explicitly log the stack trace.
        // This keeps the scheduler thread alive BUT ensures DataDog/Grafana/Logs actually catch the failure!
        scheduler.setErrorHandler(throwable ->
                log.error("❌ CRITICAL: Background Scheduled Task failed violently. Thread preserved, but manual intervention may be required.", throwable)
        );

        scheduler.initialize();
        log.info("Titanium Scheduler Engine Initialized. Pool Size: {}", poolSize);
        return scheduler;
    }
}