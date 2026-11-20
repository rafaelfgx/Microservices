package com.company.starter.configuration;

import lombok.CustomLog;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamReadRequest;
import org.springframework.util.ErrorHandler;

@CustomLog
@Configuration(proxyBeanMethods = false)
public class ConfigurationStreamConfiguration {
    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, ConfigurationEvent>> configurationStreamMessageListenerContainer(final RedisConnectionFactory connectionFactory, final ConfigurationListener listener) {
        final var container = StreamMessageListenerContainer.create(connectionFactory, StreamMessageListenerContainerOptions.builder().targetType(ConfigurationEvent.class).build());
        final var request = StreamReadRequest.builder(StreamOffset.fromStart("configurations")).cancelOnError(_ -> false).errorHandler(errorHandler()).build();
        container.register(request, listener);
        return container;
    }

    private static @NonNull ErrorHandler errorHandler() {
        return throwable -> log.at().error().marker("Configuration").message("Error").exception(throwable).log();
    }
}
