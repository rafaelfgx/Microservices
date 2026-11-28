package com.company.starter.configuration;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@CustomLog
@RequiredArgsConstructor
@Component
public class ConfigurationListener implements StreamListener<String, ObjectRecord<String, ConfigurationEvent>> {
    private final ConfigurationService configurationService;

    @Override
    public void onMessage(final ObjectRecord<String, ConfigurationEvent> event) {
        configurationService.setConfigurations(event.getValue().configurations());
        log.at().info().marker("Configuration").message("Listened").attribute("configurations", configurationService.get()).log();
    }
}
