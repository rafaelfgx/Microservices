package com.company.starter.logging.subject;

import com.company.starter.logging.LoggingConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@NullMarked
@Component
public class SubjectFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain chain) throws ServletException, IOException {
        MDC.put(LoggingConstant.SUBJECT_MDC_KEY, subject().toString());

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(LoggingConstant.SUBJECT_MDC_KEY);
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
}
