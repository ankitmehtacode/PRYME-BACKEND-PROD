package com.pryme.Backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class LifecycleConfig {

    @Bean
    public TaskScheduler taskScheduler(
            @Value("${app.runtime.scheduler.pool-size:2}") int poolSize,
            @Value("${app.runtime.scheduler.await-termination-seconds:30}") int awaitTerminationSeconds
    ) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, poolSize));
        scheduler.setThreadNamePrefix("pryme-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(Math.max(5, awaitTerminationSeconds));
        scheduler.setErrorHandler(t -> {
            // keep scheduler threads alive on task failures
        });
        return scheduler;
    }
}
