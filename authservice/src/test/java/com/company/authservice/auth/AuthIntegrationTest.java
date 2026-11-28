package com.company.authservice.auth;

import com.company.authservice.shared.Data;
import com.company.authservice.shared.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.stream.IntStream;
import java.util.stream.Stream;

class AuthIntegrationTest extends IntegrationTest {
    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
        Assertions.assertThat(auth(Data.AUTH_REQUEST_INVALID)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedWhenValidCredentialsFollowTooManyFailedAttempts() {
        final var invalid = new AuthRequest(Data.AUTH_REQUEST_VALID.username(), "invalid");
        IntStream.range(0, 5).forEach(_ -> Assertions.assertThat(auth(invalid)).hasStatus(HttpStatus.UNAUTHORIZED));
        Assertions.assertThat(auth(Data.AUTH_REQUEST_VALID)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnOkWhenCredentialsAreValid() {
        final var response = Assertions.assertThat(auth(Data.AUTH_REQUEST_VALID)).hasStatus(HttpStatus.OK).bodyJson().convertTo(AuthResponse.class).actual();
        Assertions.assertThat(response.accessToken()).isNotBlank();
        Assertions.assertThat(response.refreshToken()).isNotBlank();
        Assertions.assertThat(response.accessTokenExpiresIn()).isPositive();
        Assertions.assertThat(response.refreshTokenExpiresIn()).isPositive();
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthorizationIsMissing() {
        Assertions.assertThat(mvc.get().uri("/auth").exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnOkWhenAuthorizationIsPresent() {
        Assertions.assertThat(mvc.get().uri("/auth").header("Authorization", authorization).exchange()).hasStatus(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldRejectInvalidAuthenticationRequests(final AuthRequest request) {
        Assertions.assertThat(auth(request)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    private static Stream<AuthRequest> invalidRequests() {
        return Stream.of(new AuthRequest(null, "password"), new AuthRequest("", "password"), new AuthRequest("username", null), new AuthRequest("username", ""));
    }
}
