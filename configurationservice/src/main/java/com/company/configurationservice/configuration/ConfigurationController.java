package com.company.configurationservice.configuration;

import com.company.configurationservice.configuration.handlers.CreateConfigurationHandler;
import com.company.configurationservice.configuration.handlers.DeleteConfigurationHandler;
import com.company.configurationservice.configuration.handlers.GetConfigurationHandler;
import com.company.configurationservice.configuration.handlers.ListConfigurationHandler;
import com.company.configurationservice.configuration.handlers.UpdateConfigurationHandler;
import com.company.configurationservice.configuration.handlers.UpdateConfigurationValueHandler;
import com.company.configurationservice.configuration.requests.CreateConfigurationRequest;
import com.company.configurationservice.configuration.requests.DeleteConfigurationRequest;
import com.company.configurationservice.configuration.requests.GetConfigurationRequest;
import com.company.configurationservice.configuration.requests.UpdateConfigurationRequest;
import com.company.configurationservice.configuration.requests.UpdateConfigurationValueRequest;
import com.company.starter.configuration.Configuration;
import com.company.starter.mediator.Mediator;
import com.company.starter.swagger.DefaultApiResponse;
import com.company.starter.swagger.GetApiResponse;
import com.company.starter.swagger.PostApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Configurations")
@RequestMapping("/configurations")
@RequiredArgsConstructor
@RestController
public class ConfigurationController {
    private final Mediator mediator;

    @Operation(summary = "List")
    @GetApiResponse
    @GetMapping
    public ResponseEntity<List<Configuration>> list() {
        return mediator.handleResponse(ListConfigurationHandler.class);
    }

    @Operation(summary = "Get")
    @GetApiResponse
    @GetMapping("/{id}")
    public ResponseEntity<Configuration> get(@PathVariable final String id) {
        return mediator.handle(GetConfigurationHandler.class, new GetConfigurationRequest(id));
    }

    @Operation(summary = "Create")
    @PostApiResponse
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid final CreateConfigurationRequest request) {
        return mediator.handle(CreateConfigurationHandler.class, request);
    }

    @Operation(summary = "Update")
    @DefaultApiResponse
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final String id, @RequestBody @Valid final UpdateConfigurationRequest request) {
        return mediator.handle(UpdateConfigurationHandler.class, request.withId(id));
    }

    @Operation(summary = "Update Value")
    @DefaultApiResponse
    @PatchMapping("/{id}/value/{value}")
    public ResponseEntity<Void> updateValue(@PathVariable final String id, @PathVariable final String value) {
        return mediator.handle(UpdateConfigurationValueHandler.class, new UpdateConfigurationValueRequest(id, value));
    }

    @Operation(summary = "Delete")
    @DefaultApiResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final String id) {
        return mediator.handle(DeleteConfigurationHandler.class, new DeleteConfigurationRequest(id));
    }
}
