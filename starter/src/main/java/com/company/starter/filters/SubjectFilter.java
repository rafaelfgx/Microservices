package com.company.starter.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER + 1)
@Component
public class SubjectFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
        try (final var closeable = MDC.putCloseable("subject", subject().toString())) {
            chain.doFilter(request, response);
        }
    }

    private Subject subject() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return new Subject("anonymous", "anonymous");
        }

        final var id = StringUtils.firstNonBlank(
            jwt.getSubject(),
            jwt.getClaimAsString("client_id"),
            jwt.getClaimAsString("azp"),
            "anonymous"
        );

        final var name = StringUtils.firstNonBlank(
            jwt.getClaimAsString("preferred_username"),
            jwt.getClaimAsString("name"),
            jwt.getClaimAsString("client_name"),
            jwt.getClaimAsString("client_id"),
            jwt.getClaimAsString("azp"),
            "anonymous"
        );

        return new Subject(id, name);
    }

    public record Subject(String id, String name) {}
}
