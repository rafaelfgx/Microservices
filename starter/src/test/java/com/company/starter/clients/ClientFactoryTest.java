package com.company.starter.clients;

import com.company.starter.logging.correlation.CorrelationInterceptor;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;

class ClientFactoryTest {
    @Test
    void shouldCreateClientAndHandleErrors() throws Exception {
        final var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        try {
            server.createContext("/bad-request", exchange -> response(exchange, HttpStatus.BAD_REQUEST));
            server.createContext("/problem-detail", ClientFactoryTest::responseProblemDetail);
            server.createContext("/not-found", exchange -> response(exchange, HttpStatus.NOT_FOUND));
            server.createContext("/internal-server-error", exchange -> response(exchange, HttpStatus.INTERNAL_SERVER_ERROR));
            server.start();

            final var factory = new ClientFactory(Mockito.mock(OAuth2AuthorizedClientManager.class), new ObjectMapper(), new CorrelationInterceptor());
            final var client = factory.create(Client.class, new Properties(server.getAddress().getPort()));
            Assertions.assertNotNull(client);

            final var badRequest = Assertions.assertThrows(ErrorResponseException.class, client::badRequest);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());

            final var problemDetail = Assertions.assertThrows(ErrorResponseException.class, client::problemDetail);
            Assertions.assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, problemDetail.getStatusCode());

            Assertions.assertDoesNotThrow(client::notFound);

            final var internalServerError = Assertions.assertThrows(ErrorResponseException.class, client::internalServerError);
            Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, internalServerError.getStatusCode());
        } finally {
            server.stop(0);
        }
    }

    private static void response(final com.sun.net.httpserver.HttpExchange exchange, final HttpStatus status) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        exchange.sendResponseHeaders(status.value(), -1);
    }

    private static void responseProblemDetail(final com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        final var bytes = new JsonMapper().writeValueAsBytes(ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT));
        exchange.getResponseHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        exchange.sendResponseHeaders(HttpStatus.UNPROCESSABLE_CONTENT.value(), bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    @HttpExchange
    interface Client {
        @GetExchange("bad-request")
        void badRequest();

        @GetExchange("problem-detail")
        void problemDetail();

        @GetExchange("internal-server-error")
        void internalServerError();

        @GetExchange("not-found")
        void notFound();
    }

    private record Properties(int port) implements ClientProperties {
        @Override
        public URI url() {
            return URI.create("http://localhost:" + port);
        }

        @Override
        public Duration connectTimeout() {
            return Duration.ofSeconds(1);
        }

        @Override
        public Duration readTimeout() {
            return Duration.ofSeconds(1);
        }
    }
}
