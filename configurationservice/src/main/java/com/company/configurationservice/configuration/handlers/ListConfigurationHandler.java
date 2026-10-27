package com.company.configurationservice.configuration.handlers;

import com.company.configurationservice.configuration.ConfigurationRepository;
import com.company.starter.configuration.Configuration;
import com.company.starter.mediator.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ListConfigurationHandler implements ResponseHandler<ResponseEntity<List<Configuration>>> {
    private final ConfigurationRepository configurationRepository;

    @Override
    public ResponseEntity<List<Configuration>> handle() {
        return ResponseEntity.of(Optional.of(configurationRepository.findAllBy(Configuration.class)).filter(configurations -> !configurations.isEmpty()));
    }
}
