package com.company.starter.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConfigurationListener implements StreamListener<String, ObjectRecord<String, ConfigurationEvent>> {
    private final ConfigurationService configurationService;

    @Override
    public void onMessage(final ObjectRecord<String, ConfigurationEvent> event) {
        configurationService.setConfigurations(event.getValue().configurations());
        log.atInfo().addKeyValue(ConfigurationStreamConfiguration.KEY, configurationService.get()).log("[Configuration] Listened");
    }
}
