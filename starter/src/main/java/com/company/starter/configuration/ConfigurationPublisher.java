package com.company.starter.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConfigurationPublisher {
    private final StringRedisTemplate redisTemplate;

    public void publish(final ConfigurationEvent event) {
        final var record = StreamRecords.objectBacked(event).withStreamKey(ConfigurationStreamConfiguration.KEY);
        redisTemplate.opsForStream().add(record, XAddOptions.maxlen(1));
        log.atInfo().addKeyValue(ConfigurationStreamConfiguration.KEY, event.configurations()).log("[Configuration] Published");
    }
}
