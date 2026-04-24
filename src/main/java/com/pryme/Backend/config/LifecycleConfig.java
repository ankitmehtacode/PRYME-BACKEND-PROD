package com.pryme.Backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@EnableScheduling
public class LifecycleConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(LifecycleConfig.class);

    @Bean(destroyMethod = "close")
    public ExecutorService asyncVirtualThreadExecutor() {
        log.info("Initializing virtual-thread async executor");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor(asyncVirtualThreadExecutor());
    }

    @Bean
    public AsyncTaskExecutor asyncTaskExecutor(ExecutorService asyncVirtualThreadExecutor) {
        return new TaskExecutorAdapter(asyncVirtualThreadExecutor);
    }

    @Bean
    public TaskScheduler taskScheduler(
            @Value("${app.runtime.scheduler.pool-size:2}") int poolSize,
            @Value("${app.runtime.scheduler.await-termination-seconds:30}") int awaitTerminationSeconds
    ) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, poolSize));
        scheduler.setThreadNamePrefix("PrymeCron-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(Math.max(5, awaitTerminationSeconds));
        scheduler.setErrorHandler(throwable ->
                log.error("Scheduled task failed", throwable)
        );
        scheduler.initialize();
        return scheduler;
    }
}
