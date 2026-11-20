package com.company.starter.configuration;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@CustomLog
@RequiredArgsConstructor
@Component
public class ConfigurationPublisher {
    private final StringRedisTemplate redisTemplate;

    public void publish(final ConfigurationEvent event) {
        final var record = StreamRecords.objectBacked(event).withStreamKey("configurations");
        redisTemplate.opsForStream().add(record, XAddOptions.maxlen(1));
        log.at().info().marker("Configuration").message("Published").attribute("configurations", event.configurations()).log();
    }
}
