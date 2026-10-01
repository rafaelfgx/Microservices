package com.company.starter.configurations;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;

class RestClientConfigurationTest {
    @Test
    void shouldConfigureClientAndHandleErrors() throws Exception {
        final var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        try {
            server.createContext("/bad-request", exchange -> response(exchange, HttpStatus.BAD_REQUEST));
            server.createContext("/problem-detail", RestClientConfigurationTest::responseProblemDetail);
            server.createContext("/not-found", exchange -> response(exchange, HttpStatus.NOT_FOUND));
            server.createContext("/internal-server-error", exchange -> response(exchange, HttpStatus.INTERNAL_SERVER_ERROR));
            server.start();

            final var client = createClient("http://localhost:" + server.getAddress().getPort());

            Assertions.assertThrows(HttpClientErrorException.BadRequest.class, client::badRequest);

            final var problemDetail = Assertions.assertThrows(HttpClientErrorException.UnprocessableContent.class, client::problemDetail).getResponseBodyAs(ProblemDetail.class);
            Assertions.assertNotNull(problemDetail);
            Assertions.assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), problemDetail.getStatus());

            Assertions.assertDoesNotThrow(client::notFound);

            Assertions.assertThrows(HttpServerErrorException.InternalServerError.class, client::internalServerError);
        } finally {
            server.stop(0);
        }
    }

    private static Client createClient(final String baseUrl) {
        final var builder = RestClient.builder().baseUrl(baseUrl);
        new RestClientConfiguration().restClientCustomizer().customize(builder);
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.build())).build().createClient(Client.class);
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
}
