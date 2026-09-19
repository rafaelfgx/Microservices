package com.company.authservice.auth;

import com.company.authservice.shared.Data;
import com.company.starter.exception.ApplicationException;
import jakarta.ws.rs.core.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

class AuthServiceTest {
    @Test
    void shouldThrowConflictWhenNoUserMatchesBothUsernameAndEmailSimultaneously() {
        final var service = new AuthService(properties());
        final var email = userRepresentation("another-username", Data.USER_REQUEST.email());
        final var username = userRepresentation(Data.USER_REQUEST.username(), "another-email@mail.com");
        Assertions
            .assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "get", List.of(email, username), Data.USER_REQUEST.username(), Data.USER_REQUEST.email()))
            .isInstanceOf(ApplicationException.class);
    }

    @Test
    void shouldReturnBadRequestWhenKeycloakUserCreationFails() {
        final var service = new AuthService(properties());
        final var builder = Mockito.mock(KeycloakBuilder.class, Answers.RETURNS_SELF);
        final var keycloak = Mockito.mock(Keycloak.class);
        final var realm = Mockito.mock(RealmResource.class);
        final var users = Mockito.mock(UsersResource.class);
        final var response = Mockito.mock(Response.class);

        Mockito.when(builder.build()).thenReturn(keycloak);
        Mockito.when(keycloak.realm(Mockito.anyString())).thenReturn(realm);
        Mockito.when(realm.users()).thenReturn(users);
        Mockito.when(users.create(Mockito.any(UserRepresentation.class))).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(HttpStatus.BAD_REQUEST.value());

        try (MockedStatic<KeycloakBuilder> keycloakBuilder = Mockito.mockStatic(KeycloakBuilder.class)) {
            keycloakBuilder.when(KeycloakBuilder::builder).thenReturn(builder);
            final ResponseEntity<?> result = ReflectionTestUtils.invokeMethod(service, "create", Data.USER_REQUEST);
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            Assertions.assertThat(result.getBody()).isNull();
        }
    }

    private static AuthProperties properties() {
        final var properties = new AuthProperties();
        properties.setUrl("http://localhost");
        properties.setRealm("realm");
        properties.setClientId("client");
        properties.setClientSecret("secret");
        return properties;
    }

    private static UserRepresentation userRepresentation(final String username, final String email) {
        final var user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
}
