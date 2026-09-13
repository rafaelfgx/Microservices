package com.company.starter.logging.correlation;

import com.company.starter.logging.LoggingConstant;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

class CorrelationFilterTest {
    private final CorrelationFilter correlationFilter = new CorrelationFilter();

    @Test
    void shouldGenerateCorrelationIdAndPropagateWhenHeaderIsAbsent() throws ServletException, IOException {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var captured = new String[1];
        correlationFilter.doFilter(request, response, (req, res) -> captured[0] = MDC.get(LoggingConstant.CORRELATION_ID_MDC_KEY));
        Assertions.assertNotNull(captured[0]);
        Assertions.assertEquals(captured[0], response.getHeader(LoggingConstant.CORRELATION_ID_HEADER_NAME));
        Assertions.assertNull(MDC.get(LoggingConstant.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void shouldUseExistingCorrelationIdWhenHeaderIsPresent() throws ServletException, IOException {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var existing = UUID.randomUUID().toString();
        request.addHeader(LoggingConstant.CORRELATION_ID_HEADER_NAME, existing);
        final var captured = new String[1];
        correlationFilter.doFilter(request, response, (req, res) -> captured[0] = MDC.get(LoggingConstant.CORRELATION_ID_MDC_KEY));
        Assertions.assertEquals(existing, captured[0]);
        Assertions.assertEquals(existing, response.getHeader(LoggingConstant.CORRELATION_ID_HEADER_NAME));
        Assertions.assertNull(MDC.get(LoggingConstant.CORRELATION_ID_MDC_KEY));
    }
}
