package com.company.starter.logging.correlation;

import com.company.starter.logging.LoggingConstant;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@NullMarked
@Component
public class CorrelationInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(
        final HttpRequest request,
        final byte[] body,
        final ClientHttpRequestExecution execution) throws IOException {
        final var correlationId = MDC.get(LoggingConstant.CORRELATION_ID_MDC_KEY);

        if (StringUtils.hasText(correlationId)) {
            request.getHeaders().set(LoggingConstant.CORRELATION_ID_HEADER_NAME, correlationId);
        }

        return execution.execute(request, body);
    }
}
