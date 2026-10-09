package com.company.authservice.keycloak;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KeycloakToken(String accessToken, Long expiresIn, String refreshToken, Long refreshExpiresIn) {
}
