package com.company.starter.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@Slf4j
@EnableScheduling
@Configuration
public class SchedulerConfiguration {
    @Bean
    public TaskScheduler scheduler() {
        final var scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.setErrorHandler(throwable -> log.error("[Scheduler]", throwable));
        return scheduler;
    }
}
