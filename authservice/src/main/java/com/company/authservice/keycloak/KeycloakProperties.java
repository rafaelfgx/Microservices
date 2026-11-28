package com.company.authservice.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.http.serviceclient.keycloak")
public record KeycloakProperties(String clientId, String clientSecret) {
}
