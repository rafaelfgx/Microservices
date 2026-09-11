package com.company.starter.clients;

import com.company.starter.logging.correlation.CorrelationInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClientFactory {
    private final OAuth2AuthorizedClientManager clientManager;
    private final ObjectMapper objectMapper;
    private final CorrelationInterceptor correlationInterceptor;

    public <T> T create(final Class<T> type, final ClientProperties properties) {
        final var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());

        final var oauthInterceptor = new OAuth2ClientHttpRequestInterceptor(clientManager);
        oauthInterceptor.setClientRegistrationIdResolver(_ -> properties.registrationId());

        final var client = RestClient
            .builder()
            .baseUrl(properties.url().toString())
            .defaultStatusHandler(HttpStatus.NOT_FOUND::equals, (_, _) -> {})
            .defaultStatusHandler(HttpStatusCode::isError, this::handleError)
            .requestFactory(factory)
            .requestInterceptor(correlationInterceptor)
            .requestInterceptor(oauthInterceptor)
            .build();

        return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(client))
            .build()
            .createClient(type);
    }

    private void handleError(final HttpRequest request, final ClientHttpResponse response) throws IOException {
        final var body = new String(response.getBody().readNBytes(64 * 1024), StandardCharsets.UTF_8);

        if (response.getStatusCode().is5xxServerError()) {
            log
                .atError()
                .addKeyValue("uri", request.getURI())
                .addKeyValue("method", request.getMethod())
                .addKeyValue("status", response.getStatusCode().value())
                .addKeyValue("statusText", response.getStatusText())
                .addKeyValue("body", body)
                .log("[Http]");
        }

        ProblemDetail problemDetail;

        try {
            problemDetail = objectMapper.readValue(body, ProblemDetail.class);
        } catch (final Exception _) {
            problemDetail = ProblemDetail.forStatusAndDetail(response.getStatusCode(), body);
        }

        throw new ErrorResponseException(response.getStatusCode(), problemDetail, null);
    }
}
