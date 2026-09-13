package com.company.configurationservice.configuration.handlers;

import com.company.configurationservice.configuration.ConfigurationRepository;
import com.company.configurationservice.configuration.requests.GetConfigurationRequest;
import com.company.starter.configuration.Configuration;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetConfigurationHandler implements RequestResponseHandler<GetConfigurationRequest, ResponseEntity<Configuration>> {
    private final ConfigurationRepository configurationRepository;

    @Override
    public ResponseEntity<Configuration> handle(final GetConfigurationRequest request) {
        return ResponseEntity.of(configurationRepository.findById(request.id(), Configuration.class));
    }
}
