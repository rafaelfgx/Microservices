package com.company.starter.clients.auth;

import com.company.starter.clients.auth.dtos.SaveUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

@ClientRegistrationId("default")
@HttpExchange
public interface AuthClient {
    @PostExchange("users")
    ResponseEntity<UUID> save(@RequestBody final SaveUserRequest request);

    @DeleteExchange("users/{id}")
    ResponseEntity<Void> delete(@PathVariable final UUID id);
}
