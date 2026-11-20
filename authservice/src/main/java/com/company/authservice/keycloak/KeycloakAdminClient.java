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

import java.util.List;
import java.util.UUID;

@ClientRegistrationId("default")
@HttpExchange("/admin/realms/${spring.http.serviceclient.keycloak.realm}")
public interface KeycloakAdminClient {
    @GetExchange("/users?exact=true&max=1&briefRepresentation=true")
    List<KeycloakUser> search(@RequestParam String username, @RequestParam String email);

    @PostExchange("/users")
    ResponseEntity<Void> create(@RequestBody KeycloakUser user);

    @DeleteExchange("/users/{id}")
    void delete(@PathVariable UUID id);
}
