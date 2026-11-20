package com.company.starter.clients.auth;

import com.company.starter.clients.auth.dtos.SaveUserRequest;
import com.company.starter.resilience.DefaultConcurrencyLimit;
import com.company.starter.resilience.DefaultRetryable;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.service.registry.ImportHttpServices;

import java.util.UUID;

@ConditionalOnProperty("spring.http.serviceclient.auth.base-url")
@ImportHttpServices(group = "auth", types = AuthClient.class)
@DefaultConcurrencyLimit
@DefaultRetryable
@RequiredArgsConstructor
@Component
public class AuthAmbassador {
    private final AuthClient client;

    public ResponseEntity<UUID> save(final SaveUserRequest request) {
        return client.save(request);
    }

    public ResponseEntity<Void> delete(final UUID id) {
        return client.delete(id);
    }
}
