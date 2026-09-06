package com.company.configurationservice.configuration.handlers;

import com.company.configurationservice.configuration.Configuration;
import com.company.configurationservice.configuration.ConfigurationRepository;
import com.company.configurationservice.configuration.requests.UpdateConfigurationValueRequest;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UpdateConfigurationValueHandler implements RequestResponseHandler<UpdateConfigurationValueRequest, ResponseEntity<Void>> {
    private final ConfigurationRepository configurationRepository;
    private final PublishConfigurationHandler publishConfigurationHandler;

    @Transactional
    @Override
    public ResponseEntity<Void> handle(final UpdateConfigurationValueRequest request) {
        configurationRepository.findById(request.id()).ifPresent(configuration -> update(configuration, request));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void update(final Configuration configuration, final UpdateConfigurationValueRequest request) {
        configurationRepository.save(configuration.updateValue(request.value()));
        publishConfigurationHandler.handle();
    }
}
