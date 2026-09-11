package com.company.configurationservice.configuration.handlers;

import com.company.configurationservice.configuration.ConfigurationRepository;
import com.company.starter.configuration.Configuration;
import com.company.starter.configuration.ConfigurationEvent;
import com.company.starter.configuration.ConfigurationPublisher;
import com.company.starter.mediator.Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PublishConfigurationHandler implements Handler {
    private final ConfigurationRepository configurationRepository;
    private final ConfigurationPublisher configurationPublisher;

    @Override
    public void handle() {
        configurationPublisher.publish(new ConfigurationEvent(configurationRepository.findAllBy(Configuration.class)));
    }
}
