package com.company.authservice.keycloak;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record KeycloakUser(
    UUID id,
    String firstName,
    String lastName,
    String username,
    String email,
    Boolean emailVerified,
    Boolean enabled,
    List<KeycloakCredential> credentials) {
}
