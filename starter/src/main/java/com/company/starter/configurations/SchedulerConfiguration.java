package com.company.starter.configurations;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@Slf4j
@EnableSchedulerLock(defaultLockAtMostFor = "PT1M")
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

    @Bean
    LockProvider schedulerLockProvider(final MongoTemplate mongoTemplate) {
        return new MongoLockProvider(mongoTemplate.getDb());
    }
}
