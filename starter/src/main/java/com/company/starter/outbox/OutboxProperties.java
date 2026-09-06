package com.company.starter.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "outbox")
public record OutboxProperties(Duration delay, Duration lock, int limit, int attempts) {
}
