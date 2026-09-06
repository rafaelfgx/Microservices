package com.company.starter.logging.correlation;

import com.company.starter.logging.LoggingConstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

class CorrelationInterceptorTest {
    private final CorrelationInterceptor correlationInterceptor = new CorrelationInterceptor();

    @Test
    void shouldPropagateCorrelationIdWhenPresentInMdc() throws Exception {
        final var request = Mockito.mock(HttpRequest.class);
        final var execution = Mockito.mock(ClientHttpRequestExecution.class);
        final var headers = new HttpHeaders();

        Mockito.when(request.getHeaders()).thenReturn(headers);
        MDC.put(LoggingConstant.CORRELATION_ID_MDC_KEY, "id");

        try (final var ignored = correlationInterceptor.intercept(request, new byte[0], execution)) {
            Assertions.assertEquals("id", headers.getFirst(LoggingConstant.CORRELATION_ID_HEADER_NAME));
        }

        MDC.remove(LoggingConstant.CORRELATION_ID_MDC_KEY);
    }

    @Test
    void shouldNotAddHeaderWhenCorrelationIdIsAbsentInMdc() throws Exception {
        final var request = Mockito.mock(HttpRequest.class);
        final var execution = Mockito.mock(ClientHttpRequestExecution.class);
        final var headers = new HttpHeaders();

        Mockito.when(request.getHeaders()).thenReturn(headers);
        MDC.remove(LoggingConstant.CORRELATION_ID_MDC_KEY);

        try (final var ignored = correlationInterceptor.intercept(request, new byte[0], execution)) {
            Assertions.assertNull(headers.getFirst(LoggingConstant.CORRELATION_ID_HEADER_NAME));
        }
    }
}
