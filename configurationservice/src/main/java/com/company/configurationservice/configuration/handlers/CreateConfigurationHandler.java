package com.company.configurationservice.configuration.handlers;

import com.company.configurationservice.configuration.ConfigurationMapper;
import com.company.configurationservice.configuration.ConfigurationRepository;
import com.company.configurationservice.configuration.requests.CreateConfigurationRequest;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CreateConfigurationHandler implements RequestResponseHandler<CreateConfigurationRequest, ResponseEntity<Void>> {
    private final ConfigurationMapper configurationMapper;
    private final ConfigurationRepository configurationRepository;
    private final PublishConfigurationHandler publishConfigurationHandler;

    @Transactional
    @Override
    public ResponseEntity<Void> handle(final CreateConfigurationRequest request) {
        configurationRepository.save(configurationMapper.toConfiguration(request));
        publishConfigurationHandler.handle();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
