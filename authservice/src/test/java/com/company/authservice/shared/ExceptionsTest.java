package com.company.authservice.shared;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ExceptionsTest {
    private final Exceptions exceptions = new Exceptions();

    @Test
    void shouldReturnUnauthorizedWhenNotAuthorizedExceptionOccurs() {
        final var response = exceptions.handle(new NotAuthorizedException("NotAuthorizedException"));
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnForbiddenWhenForbiddenExceptionOccurs() {
        final var response = exceptions.handle(new ForbiddenException("ForbiddenException"));
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
