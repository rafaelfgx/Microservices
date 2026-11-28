package com.company.authservice.user;

import com.company.authservice.auth.AuthRequest;
import com.company.authservice.shared.Data;
import com.company.authservice.shared.IntegrationTest;
import com.company.authservice.shared.KeycloakTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;
import java.util.stream.Stream;

class UserIntegrationTest extends IntegrationTest {
    private static final String CUSTOMER_CLIENT = "customerservice";

    @Test
    void shouldRejectUserManagementWithoutRequiredRoles() {
        for (final var token : new String[]{authorization, bearerToken("authservice"), bearerToken("configurationservice"), bearerToken("productservice"), bearerToken("orderservice"), bearerToken("paymentservice")}) {
            Assertions.assertThat(save(token, Data.SAVE_USER_REQUEST)).hasStatus(HttpStatus.FORBIDDEN);
            Assertions.assertThat(verify(token, UUID.randomUUID())).hasStatus(HttpStatus.FORBIDDEN);
            Assertions.assertThat(delete(token, UUID.randomUUID())).hasStatus(HttpStatus.FORBIDDEN);
        }
    }

    @Test
    void shouldAllowCustomerServiceToManageUsers() {
        final var token = bearerToken(CUSTOMER_CLIENT);
        final var userId = id(save(token, Data.SAVE_USER_REQUEST));
        Assertions.assertThat(verify(token, userId)).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(delete(token, userId)).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldChangePasswordAfterCheckingCurrentPassword() {
        final var request = new ChangePasswordRequest(null, Data.AUTH_REQUEST_VALID.password(), "newpassword");
        Assertions.assertThat(changePassword(request)).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(auth(new AuthRequest(Data.AUTH_REQUEST_VALID.username(), "newpassword"))).hasStatus(HttpStatus.OK);
        Assertions.assertThat(auth(Data.AUTH_REQUEST_VALID)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @MethodSource("requestsReusingUsernameOrEmail")
    void shouldOverwriteUnverifiedUserWhenUsernameOrEmailIsReused(final SaveUserRequest request) {
        final var id = id(save(Data.SAVE_USER_REQUEST));
        Assertions.assertThat(id(save(request))).isEqualTo(id);
        Assertions.assertThat(verify(id)).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(auth(request)).hasStatus(HttpStatus.OK);
        Assertions.assertThat(auth(Data.SAVE_USER_REQUEST)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnConflictWhenUsernameAndEmailBelongToDifferentUsers() {
        id(save(Data.SAVE_USER_REQUEST));
        id(save(new SaveUserRequest("another@mail.com", "another", Data.PASSWORD)));
        Assertions.assertThat(save(new SaveUserRequest("another@mail.com", Data.USERNAME, Data.PASSWORD))).hasStatus(HttpStatus.CONFLICT);
    }

    @ParameterizedTest
    @MethodSource("requestsReusingUsernameOrEmail")
    void shouldReturnConflictWhenUsernameOrEmailBelongsToVerifiedUser(final SaveUserRequest request) {
        Assertions.assertThat(verify(id(save(Data.SAVE_USER_REQUEST)))).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(save(request)).hasStatus(HttpStatus.CONFLICT);
        Assertions.assertThat(auth(Data.SAVE_USER_REQUEST)).hasStatus(HttpStatus.OK);
        Assertions.assertThat(auth(request)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnConflictWhenSavingUserWithVerifiedEmail() {
        final var request = new SaveUserRequest("admin@mail.com", Data.AUTH_REQUEST_VALID.username(), "newpassword");
        Assertions.assertThat(save(request)).hasStatus(HttpStatus.CONFLICT);
        Assertions.assertThat(auth(Data.AUTH_REQUEST_VALID)).hasStatus(HttpStatus.OK);
        Assertions.assertThat(auth(request)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnConflictWhenUsernameBelongsToServiceAccount() {
        Assertions.assertThat(save(new SaveUserRequest(Data.EMAIL, "service-account-" + CUSTOMER_CLIENT, Data.PASSWORD))).hasStatus(HttpStatus.CONFLICT);
        Assertions.assertThat(save(Data.SAVE_USER_REQUEST)).hasStatus(HttpStatus.OK);
    }

    @Test
    void shouldAuthenticateOnlyAfterUserIsVerified() {
        final var id = id(save(Data.SAVE_USER_REQUEST));
        Assertions.assertThat(auth(Data.SAVE_USER_REQUEST)).hasStatus(HttpStatus.UNAUTHORIZED);
        Assertions.assertThat(verify(id)).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(auth(Data.SAVE_USER_REQUEST)).hasStatus(HttpStatus.OK);
    }

    @Test
    void shouldReturnNoContentWhenVerifyingUnknownUser() {
        Assertions.assertThat(verify(UUID.randomUUID())).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturnNoContentWhenDeletingUser() {
        Assertions.assertThat(delete(UUID.randomUUID())).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(delete(id(save(Data.SAVE_USER_REQUEST)))).hasStatus(HttpStatus.NO_CONTENT);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldRejectInvalidUserRequests(final SaveUserRequest request) {
        Assertions.assertThat(save(request)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldRejectUserCreationWithoutAuthorization() {
        Assertions.assertThat(mvc.post().uri("/users").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Data.SAVE_USER_REQUEST)).exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUserVerificationWithoutAuthorization() {
        Assertions.assertThat(mvc.patch().uri("/users/{id}/verify", UUID.randomUUID()).exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUserDeletionWithoutAuthorization() {
        Assertions.assertThat(mvc.delete().uri("/users/{id}", UUID.randomUUID()).exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private static Stream<SaveUserRequest> requestsReusingUsernameOrEmail() {
        return Stream.of(new SaveUserRequest(Data.EMAIL, Data.USERNAME, "newpassword"), new SaveUserRequest("another@mail.com", Data.USERNAME, "newpassword"), new SaveUserRequest(Data.EMAIL, "another", "newpassword"), new SaveUserRequest("EMAIL@MAIL.COM", "USERNAME", "newpassword"));
    }

    private static Stream<SaveUserRequest> invalidRequests() {
        return Stream.of(new SaveUserRequest(null, Data.USERNAME, Data.PASSWORD), new SaveUserRequest("", Data.USERNAME, Data.PASSWORD), new SaveUserRequest("invalid", Data.USERNAME, Data.PASSWORD), new SaveUserRequest(Data.EMAIL, null, Data.PASSWORD), new SaveUserRequest(Data.EMAIL, "", Data.PASSWORD), new SaveUserRequest(Data.EMAIL, Data.USERNAME, null), new SaveUserRequest(Data.EMAIL, Data.USERNAME, ""));
    }

    private MvcTestResult auth(final SaveUserRequest request) {
        return auth(new AuthRequest(request.username(), request.password()));
    }

    private MvcTestResult save(final SaveUserRequest request) {
        return save(bearerToken(CUSTOMER_CLIENT), request);
    }

    private MvcTestResult save(final String token, final SaveUserRequest request) {
        return mvc.post().uri("/users").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)).exchange();
    }

    private MvcTestResult verify(final UUID id) {
        return verify(bearerToken(CUSTOMER_CLIENT), id);
    }

    private MvcTestResult verify(final String token, final UUID id) {
        return mvc.patch().uri("/users/{id}/verify", id).header("Authorization", token).exchange();
    }

    private MvcTestResult changePassword(final ChangePasswordRequest request) {
        return mvc.patch().uri("/users/change/password").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)).exchange();
    }

    private MvcTestResult delete(final UUID id) {
        return delete(bearerToken(CUSTOMER_CLIENT), id);
    }

    private MvcTestResult delete(final String token, final UUID id) {
        return mvc.delete().uri("/users/{id}", id).header("Authorization", token).exchange();
    }

    private static String bearerToken(final String clientId) {
        return "Bearer " + KeycloakTestConfiguration.serviceToken(clientId, clientId);
    }

    private UUID id(final MvcTestResult result) {
        return Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().convertTo(UUID.class).actual();
    }
}
