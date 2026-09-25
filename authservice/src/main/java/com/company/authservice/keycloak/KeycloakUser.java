package com.company.authservice.keycloak;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record KeycloakUser(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    Boolean enabled,
    Boolean emailVerified,
    List<KeycloakCredential> credentials) {
}
