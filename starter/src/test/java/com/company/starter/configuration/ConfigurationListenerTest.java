package com.company.starter.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.data.redis.connection.stream.ObjectRecord;

import java.util.List;
import java.util.Optional;

class ConfigurationListenerTest {
    private final ConfigurationService configurationService = new ConfigurationService(new ApplicationConversionService());
    private final ConfigurationListener configurationListener = new ConfigurationListener(configurationService);

    private void listen(final List<Configuration> configurations) {
        configurationListener.onMessage(ObjectRecord.create(ConfigurationStreamConfiguration.KEY, new ConfigurationEvent(configurations)));
    }

    @Test
    void shouldSetConfigurationsWhenMessageIsListened() {
        listen(List.of(new Configuration("id", "value", "description", "group")));
        Assertions.assertEquals(Optional.of("value"), configurationService.getValue("id", String.class));
    }

    @Test
    void shouldReplaceConfigurationsWhenNewMessageIsListened() {
        listen(List.of(new Configuration("id", "old", "description", "group")));
        listen(List.of(new Configuration("id", "new", "description", "group")));
        Assertions.assertEquals(Optional.of("new"), configurationService.getValue("id", String.class));
    }
}
