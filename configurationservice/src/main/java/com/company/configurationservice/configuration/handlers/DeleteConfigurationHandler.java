package com.company.configurationservice.configuration.handlers;

import com.company.configurationservice.configuration.ConfigurationRepository;
import com.company.configurationservice.configuration.requests.DeleteConfigurationRequest;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class DeleteConfigurationHandler implements RequestResponseHandler<DeleteConfigurationRequest, ResponseEntity<Void>> {
    private final ConfigurationRepository configurationRepository;
    private final PublishConfigurationHandler publishConfigurationHandler;

    @Transactional
    @Override
    public ResponseEntity<Void> handle(final DeleteConfigurationRequest request) {
        configurationRepository.deleteById(request.id());
        publishConfigurationHandler.handle();
        return ResponseEntity.noContent().build();
    }
}
