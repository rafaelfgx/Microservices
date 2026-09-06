package com.company.starter.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

class ConfigurationPublisherTest {
    private final StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
    private final StreamOperations<String, Object, Object> streamOperations = Mockito.mock();
    private final ConfigurationPublisher configurationPublisher = new ConfigurationPublisher(redisTemplate);

    @Test
    void shouldAddRecordWithLatestConfigurationsWhenPublishing() {
        Mockito.when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        final var configurations = List.of(new Configuration("id", "value", "description", "group"));
        configurationPublisher.publish(new ConfigurationEvent(configurations));
        final ArgumentCaptor<Record<String, Object>> recordCaptor = ArgumentCaptor.captor();
        final ArgumentCaptor<XAddOptions> optionsCaptor = ArgumentCaptor.captor();
        Mockito.verify(streamOperations).add(recordCaptor.capture(), optionsCaptor.capture());
        Assertions.assertEquals(ConfigurationStreamConfiguration.KEY, recordCaptor.getValue().getStream());
        Assertions.assertEquals(new ConfigurationEvent(configurations), recordCaptor.getValue().getValue());
        Assertions.assertEquals(Long.valueOf(1), optionsCaptor.getValue().getMaxlen());
    }
}
