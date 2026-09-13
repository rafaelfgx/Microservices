package com.company.authservice.auth;

import com.company.authservice.shared.Data;
import com.company.authservice.shared.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class AuthIntegrationTest extends IntegrationTest {
    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
        Assertions.assertThat(auth(Data.AUTH_REQUEST_INVALID)).hasStatus(UNAUTHORIZED);
    }

    @Test
    void shouldReturnOkWhenCredentialsAreValid() {
        Assertions.assertThat(auth(Data.AUTH_REQUEST_VALID)).hasStatus(OK);
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthorizationIsMissing() {
        Assertions.assertThat(mvc.get().uri("/auth").exchange()).hasStatus(UNAUTHORIZED);
    }

    @Test
    void shouldReturnOkWhenAuthorizationIsPresent() {
        Assertions.assertThat(mvc.get().uri("/auth").header("Authorization", authorization()).exchange()).hasStatus(OK);
    }
}
