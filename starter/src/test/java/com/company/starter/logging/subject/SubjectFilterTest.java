package com.company.starter.logging.subject;

import com.company.starter.logging.LoggingConstant;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

class SubjectFilterTest {
    private final SubjectFilter subjectFilter = new SubjectFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.remove(LoggingConstant.SUBJECT_MDC_KEY);
    }

    @Test
    void shouldPutAuthenticatedUserInMdc() throws ServletException, IOException {
        final var id = UUID.randomUUID().toString();
        assertSubject(new JwtAuthenticationToken(jwt(Map.of("sub", id, "preferred_username", "name"))), "Subject[id=" + id + ", name=name]");
    }

    @Test
    void shouldPutAnonymousWhenNoAuthenticationExists() throws ServletException, IOException {
        assertSubject(null, "Subject[id=anonymous, name=anonymous]");
    }

    @Test
    void shouldPutClientInMdcWhenJwtHasClientOnlyClaims() throws ServletException, IOException {
        assertSubject(new JwtAuthenticationToken(jwt(Map.of("sub", "sub", "client_id", "id", "client_name", "name"))), "Subject[id=sub, name=name]");
    }

    @Test
    void shouldPreferUserNameWhenJwtHasUserAndClientClaims() throws ServletException, IOException {
        assertSubject(new JwtAuthenticationToken(jwt(Map.of("sub", "sub", "preferred_username", "Name", "client_id", "id", "client_name", "name"))), "Subject[id=sub, name=Name]");
    }

    @Test
    void shouldFallbackToAnonymousWhenJwtHasNoRelevantClaims() throws ServletException, IOException {
        assertSubject(new JwtAuthenticationToken(jwt(Map.of("scope", "read"))), "Subject[id=anonymous, name=anonymous]");
    }

    @Test
    void shouldPutAnonymousWhenPrincipalIsNotJwt() throws ServletException, IOException {
        assertSubject(new TestingAuthenticationToken("subject", "password"), "Subject[id=anonymous, name=anonymous]");
    }

    private void assertSubject(final Authentication authentication, final String subject) throws ServletException, IOException {
        if (authentication == null) {
            SecurityContextHolder.clearContext();
        } else {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        final var captured = new String[1];
        subjectFilter.doFilter(request, response, (req, res) -> captured[0] = MDC.get(LoggingConstant.SUBJECT_MDC_KEY));
        Assertions.assertEquals(subject, captured[0]);
        Assertions.assertNull(MDC.get(LoggingConstant.SUBJECT_MDC_KEY));
    }

    private Jwt jwt(final Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), claims);
    }
}
