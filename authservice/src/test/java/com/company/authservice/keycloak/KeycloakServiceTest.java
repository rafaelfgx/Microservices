package com.company.authservice.keycloak;

import com.company.authservice.auth.AuthError;
import com.company.authservice.auth.AuthResponse;
import com.company.authservice.shared.Data;
import com.company.starter.exception.ApplicationException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.List;
import java.util.Map;

class KeycloakServiceTest {
    private final KeycloakAdminClient adminClient = Mockito.mock();
    private final KeycloakTokenClient tokenClient = Mockito.mock();
    private final KeycloakService service = new KeycloakService(Data.KEYCLOAK_PROPERTIES, adminClient, tokenClient);

    @Test
    void shouldReturnTokensWhenCredentialsAreValid() {
        final var form = MultiValueMap.fromSingleValue(Map.of("grant_type", "password", "client_id", Data.KEYCLOAK_PROPERTIES.clientId(), "client_secret", Data.KEYCLOAK_PROPERTIES.clientSecret(), "username", Data.AUTH_REQUEST_VALID.username(), "password", Data.AUTH_REQUEST_VALID.password()));
        Mockito.when(tokenClient.token(form)).thenReturn(new KeycloakToken("access", 300L, "refresh", 1800L));
        Assertions.assertThat(service.auth(Data.AUTH_REQUEST_VALID)).isEqualTo(new AuthResponse("access", 300L, "refresh", 1800L));
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"UNAUTHORIZED", "BAD_REQUEST"})
    void shouldThrowInvalidCredentialsWhenTokenIsRejected(final HttpStatus status) {
        Mockito.when(tokenClient.token(Mockito.any())).thenThrow(httpClientErrorException(status));
        assertApplicationException(() -> service.auth(Data.AUTH_REQUEST_INVALID), AuthError.INVALID_CREDENTIALS);
    }

    @Test
    void shouldPropagateWhenTokenEndpointFailsUnexpectedly() {
        Mockito.when(tokenClient.token(Mockito.any())).thenThrow(httpClientErrorException(HttpStatus.FORBIDDEN));
        Assertions.assertThatThrownBy(() -> service.auth(Data.AUTH_REQUEST_VALID)).isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    void shouldReturnIdWhenUserIsCreated() {
        final var headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost/admin/realms/realm/users/" + Data.ID));
        Mockito.when(adminClient.create(Data.KEYCLOAK_USER)).thenReturn(new ResponseEntity<>(headers, HttpStatus.CREATED));
        Assertions.assertThat(service.save(Data.SAVE_USER_REQUEST)).isEqualTo(Data.ID);
        Mockito.verify(adminClient, Mockito.never()).search(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void shouldReturnExistingIdWhenSameUsernameAndEmailAlreadyExist() {
        Mockito.when(adminClient.create(Data.KEYCLOAK_USER)).thenThrow(httpClientErrorException(HttpStatus.CONFLICT));
        Mockito.when(adminClient.search(Data.SAVE_USER_REQUEST.username(), Data.SAVE_USER_REQUEST.email())).thenReturn(List.of(KeycloakUser.builder().id(Data.ID).build()));
        Assertions.assertThat(service.save(Data.SAVE_USER_REQUEST)).isEqualTo(Data.ID);
    }

    @Test
    void shouldThrowConflictWhenNoUserMatchesBothUsernameAndEmail() {
        Mockito.when(adminClient.create(Data.KEYCLOAK_USER)).thenThrow(httpClientErrorException(HttpStatus.CONFLICT));
        Mockito.when(adminClient.search(Data.SAVE_USER_REQUEST.username(), Data.SAVE_USER_REQUEST.email())).thenReturn(List.of());
        assertApplicationException(() -> service.save(Data.SAVE_USER_REQUEST), AuthError.USER_EXISTS);
    }

    @Test
    void shouldPropagateWhenUserCreationFailsUnexpectedly() {
        Mockito.when(adminClient.create(Data.KEYCLOAK_USER)).thenThrow(httpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThatThrownBy(() -> service.save(Data.SAVE_USER_REQUEST)).isInstanceOf(HttpClientErrorException.BadRequest.class);
        Mockito.verify(adminClient, Mockito.never()).search(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void shouldDelegateDelete() {
        service.delete(Data.ID);
        Mockito.verify(adminClient).delete(Data.ID);
    }

    private static void assertApplicationException(final ThrowingCallable callable, final AuthError error) {
        Assertions.assertThatThrownBy(callable).isInstanceOf(ApplicationException.class).extracting("statusCode").isEqualTo(error.getStatus());
    }

    private static HttpClientErrorException httpClientErrorException(final HttpStatus status) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), null, null, null);
    }
}
