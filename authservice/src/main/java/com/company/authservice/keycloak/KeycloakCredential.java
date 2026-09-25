package com.company.authservice.keycloak;

public record KeycloakCredential(String type, String value, boolean temporary) {
    public static KeycloakCredential password(final String value) {
        return new KeycloakCredential("password", value, false);
    }
}
