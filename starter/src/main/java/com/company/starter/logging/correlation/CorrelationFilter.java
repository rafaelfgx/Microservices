package com.company.starter.logging.correlation;

import com.company.starter.logging.LoggingConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@NullMarked
@Component
public class CorrelationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain chain) throws ServletException, IOException {
        final var header = request.getHeader(LoggingConstant.CORRELATION_ID_HEADER_NAME);
        final var correlationId = StringUtils.hasText(header) ? header : UUID.randomUUID().toString();
        MDC.put(LoggingConstant.CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(LoggingConstant.CORRELATION_ID_HEADER_NAME, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(LoggingConstant.CORRELATION_ID_MDC_KEY);
        }
    }
}
