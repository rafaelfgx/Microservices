package com.company.starter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamReadRequest;

@Configuration
public class ConfigurationStreamConfiguration {
    static final String KEY = "configurations";

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, ConfigurationEvent>> configurationStreamListenerContainer(
        final RedisConnectionFactory connection,
        final ConfigurationListener listener) {
        final var options = StreamMessageListenerContainerOptions.builder().targetType(ConfigurationEvent.class).build();
        final var container = StreamMessageListenerContainer.create(connection, options);
        container.register(StreamReadRequest.builder(StreamOffset.fromStart(KEY)).cancelOnError(throwable -> false).build(), listener);
        return container;
    }
}
