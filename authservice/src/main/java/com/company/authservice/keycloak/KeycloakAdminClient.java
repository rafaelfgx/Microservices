package com.company.authservice.keycloak;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.UUID;

@ClientRegistrationId("default")
@HttpExchange("/admin/realms/${spring.http.serviceclient.keycloak.realm}")
public interface KeycloakAdminClient {
    @GetExchange("/users?exact=true&briefRepresentation=true")
    List<KeycloakUser> searchByUsername(@RequestParam final String username);

    @GetExchange("/users?exact=true&briefRepresentation=true")
    List<KeycloakUser> searchByEmail(@RequestParam final String email);

    @GetExchange("/users/{id}")
    KeycloakUser get(@PathVariable final UUID id);

    @PostExchange("/users")
    ResponseEntity<Void> create(@RequestBody final KeycloakUser user);

    @PutExchange("/users/{id}")
    void update(@PathVariable final UUID id, @RequestBody final KeycloakUser user);

    @PutExchange("/users/{id}/reset-password")
    void resetPassword(@PathVariable final UUID id, @RequestBody final KeycloakCredential credential);

    @DeleteExchange("/users/{id}")
    void delete(@PathVariable final UUID id);
}
