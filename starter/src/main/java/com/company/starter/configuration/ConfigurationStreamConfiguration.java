package com.company.starter.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamReadRequest;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class ConfigurationStreamConfiguration {
    static final String KEY = "configurations";

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, ConfigurationEvent>> configurationStreamMessageListenerContainer(final RedisConnectionFactory connectionFactory, final ConfigurationListener listener) {
        final var options = StreamMessageListenerContainerOptions.builder().targetType(ConfigurationEvent.class).build();
        final var container = StreamMessageListenerContainer.create(connectionFactory, options);
        final var request = StreamReadRequest.builder(StreamOffset.fromStart(KEY)).cancelOnError(_ -> false).errorHandler(throwable -> log.error("[Configuration]", throwable)).build();
        container.register(request, listener);
        return container;
    }
}
