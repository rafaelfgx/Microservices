package com.company.authservice.keycloak;

import com.company.authservice.auth.AuthResponse;
import com.company.authservice.shared.Data;
import com.company.authservice.shared.Error;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class KeycloakServiceTest {
    private final JwtDecoder jwtDecoder = Mockito.mock();
    private final KeycloakAdminClient keycloakAdminClient = Mockito.mock();
    private final KeycloakTokenClient keycloakTokenClient = Mockito.mock();
    private final KeycloakService keycloakService = new KeycloakService(keycloakAdminClient, keycloakTokenClient, Data.KEYCLOAK_PROPERTIES, jwtDecoder);

    @Test
    void shouldReturnTokensWhenCredentialsAreValid() {
        final var form = MultiValueMap.fromSingleValue(Map.of(
            "grant_type", "password",
            "client_id", Data.KEYCLOAK_PROPERTIES.clientId(),
            "client_secret", Data.KEYCLOAK_PROPERTIES.clientSecret(),
            "username", Data.AUTH_REQUEST_VALID.username(),
            "password", Data.AUTH_REQUEST_VALID.password()
        ));
        Mockito.when(keycloakTokenClient.token(form)).thenReturn(new KeycloakToken("access", 300L, "refresh", 1800L));
        Assertions.assertThat(keycloakService.auth(Data.AUTH_REQUEST_VALID)).isEqualTo(new AuthResponse("access", 300L, "refresh", 1800L));
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"UNAUTHORIZED", "BAD_REQUEST"})
    void shouldThrowInvalidCredentialsWhenTokenIsRejected(final HttpStatus status) {
        Mockito.when(keycloakTokenClient.token(Mockito.any())).thenThrow(HttpClientErrorException.create(status, status.getReasonPhrase(), null, null, null));
        assertErrorResponse(() -> keycloakService.auth(Data.AUTH_REQUEST_INVALID), Error.INVALID_CREDENTIALS);
    }

    @Test
    void shouldPropagateWhenTokenEndpointFailsUnexpectedly() {
        Mockito.when(keycloakTokenClient.token(Mockito.any())).thenThrow(HttpClientErrorException.Forbidden.class);
        Assertions
            .assertThatThrownBy(() -> keycloakService.auth(Data.AUTH_REQUEST_VALID))
            .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    void shouldReturnIdWhenUserIsCreated() {
        final var location = URI.create("http://localhost/admin/realms/realm/users/" + Data.ID);
        Mockito.when(keycloakAdminClient.create(keycloakUser())).thenReturn(ResponseEntity.created(location).build());
        Assertions.assertThat(keycloakService.save(Data.SAVE_USER_REQUEST)).isEqualTo(Data.ID);
        Mockito.verify(keycloakAdminClient, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true"})
    void shouldOverwriteUnverifiedUserMatchingUsernameOrEmail(final boolean matchesUsername, final boolean matchesEmail) {
        final var owner = user(Data.SAVE_USER_REQUEST.email(), false);
        conflict(matchesUsername ? List.of(owner) : List.of(), matchesEmail ? List.of(owner) : List.of());
        Assertions.assertThat(keycloakService.save(Data.SAVE_USER_REQUEST)).isEqualTo(owner.id());
        Mockito.verify(keycloakAdminClient).update(Mockito.eq(owner.id()), keycloakUser());
    }

    @Test
    void shouldThrowConflictWhenUserIsVerified() {
        conflict(List.of(user(Data.SAVE_USER_REQUEST.email(), true)), List.of());
        assertConflict();
    }

    @Test
    void shouldThrowConflictWhenUserHasNoEmail() {
        conflict(List.of(user(null, false)), List.of());
        assertConflict();
    }

    @Test
    void shouldThrowConflictWhenUsernameAndEmailBelongToDifferentUsers() {
        conflict(List.of(user("another@mail.com", false)), List.of(user(Data.SAVE_USER_REQUEST.email(), false)));
        assertConflict();
    }

    @Test
    void shouldThrowConflictWhenNoUserMatchesUsernameOrEmail() {
        conflict(List.of(), List.of());
        assertConflict();
    }

    @Test
    void shouldPropagateWhenUserCreationFailsUnexpectedly() {
        Mockito.when(keycloakAdminClient.create(keycloakUser())).thenThrow(HttpClientErrorException.BadRequest.class);
        Assertions.assertThatThrownBy(() -> keycloakService.save(Data.SAVE_USER_REQUEST)).isInstanceOf(HttpClientErrorException.BadRequest.class);
        Mockito.verify(keycloakAdminClient, Mockito.never()).searchByUsername(Mockito.anyString());
    }

    @Test
    void shouldPropagateWhenUserSearchFails() {
        Mockito.when(keycloakAdminClient.create(keycloakUser())).thenThrow(HttpClientErrorException.Conflict.class);
        Mockito.when(keycloakAdminClient.searchByUsername(Data.SAVE_USER_REQUEST.username())).thenThrow(HttpClientErrorException.Forbidden.class);
        Assertions.assertThatThrownBy(() -> keycloakService.save(Data.SAVE_USER_REQUEST)).isInstanceOf(HttpClientErrorException.Forbidden.class);
        Mockito.verify(keycloakAdminClient, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldPropagateWhenUserUpdateFails() {
        final var owner = user(Data.SAVE_USER_REQUEST.email(), false);
        conflict(List.of(owner), List.of());
        Mockito.doThrow(HttpClientErrorException.Forbidden.class).when(keycloakAdminClient).update(Mockito.eq(owner.id()), Mockito.any());
        Assertions.assertThatThrownBy(() -> keycloakService.save(Data.SAVE_USER_REQUEST)).isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    void shouldMarkEmailAsVerifiedWhenUserIsVerified() {
        keycloakService.verify(Data.ID);
        Mockito.verify(keycloakAdminClient).update(Data.ID, KeycloakUser.builder().emailVerified(true).build());
    }

    @Test
    void shouldPropagateWhenUserVerificationFails() {
        Mockito.doThrow(HttpClientErrorException.Forbidden.class).when(keycloakAdminClient).update(Mockito.eq(Data.ID), Mockito.any());
        Assertions.assertThatThrownBy(() -> keycloakService.verify(Data.ID)).isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    void shouldDelegateDelete() {
        keycloakService.delete(Data.ID);
        Mockito.verify(keycloakAdminClient).delete(Data.ID);
    }

    @Test
    void shouldPropagateWhenUserDeletionFails() {
        Mockito.doThrow(HttpClientErrorException.Forbidden.class).when(keycloakAdminClient).delete(Data.ID);
        Assertions.assertThatThrownBy(() -> keycloakService.delete(Data.ID)).isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    void shouldChangePasswordAfterVerifyingCurrentPassword() {
        authenticate(Data.ID.toString());
        keycloakService.changePassword(Data.CHANGE_PASSWORD_REQUEST);
        Mockito.verify(keycloakAdminClient).resetPassword(Data.ID, KeycloakCredential.password(Data.CHANGE_PASSWORD_REQUEST.newPassword()));
    }

    @Test
    void shouldRejectPasswordWhenAuthenticatedTokenBelongsToAnotherUser() {
        authenticate("another-user");
        assertErrorResponse(() -> keycloakService.changePassword(Data.CHANGE_PASSWORD_REQUEST), Error.INVALID_CREDENTIALS);
        Mockito.verify(keycloakAdminClient, Mockito.never()).resetPassword(Mockito.any(), Mockito.any());
    }

    private void authenticate(final String subject) {
        Mockito.when(keycloakAdminClient.get(Data.ID)).thenReturn(KeycloakUser.builder().username("username").build());
        Mockito.when(keycloakTokenClient.token(Mockito.any())).thenReturn(new KeycloakToken("access", 300L, "refresh", 1800L));
        Mockito.when(jwtDecoder.decode("access")).thenReturn(Jwt.withTokenValue("access").header("alg", "none").subject(subject).build());
    }

    private void conflict(final List<KeycloakUser> usernameOwners, final List<KeycloakUser> emailOwners) {
        Mockito.when(keycloakAdminClient.create(keycloakUser())).thenThrow(HttpClientErrorException.Conflict.class);
        Mockito.when(keycloakAdminClient.searchByUsername(Data.SAVE_USER_REQUEST.username())).thenReturn(usernameOwners);
        Mockito.when(keycloakAdminClient.searchByEmail(Data.SAVE_USER_REQUEST.email())).thenReturn(emailOwners);
    }

    private void assertConflict() {
        assertErrorResponse(() -> keycloakService.save(Data.SAVE_USER_REQUEST), Error.USER_ALREADY_EXISTS);
        Mockito.verify(keycloakAdminClient, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    private static KeycloakUser user(final String email, final boolean emailVerified) {
        return KeycloakUser.builder().id(UUID.randomUUID()).email(email).emailVerified(emailVerified).build();
    }

    private static KeycloakUser keycloakUser() {
        return Mockito.refEq(Data.KEYCLOAK_USER, "firstName", "lastName");
    }

    private static void assertErrorResponse(final ThrowingCallable callable, final Error error) {
        Assertions
            .assertThatThrownBy(callable)
            .isInstanceOfSatisfying(ErrorResponseException.class, exception -> {
                Assertions.assertThat(exception.getStatusCode()).isEqualTo(error.getStatus());
                Assertions.assertThat(exception.getBody().getDetail()).isEqualTo(error.getMessage());
            });
    }
}
