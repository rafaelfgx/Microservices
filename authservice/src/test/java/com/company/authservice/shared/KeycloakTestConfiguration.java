package com.company.authservice.shared;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Objects;

@TestConfiguration(proxyBeanMethods = false)
public class KeycloakTestConfiguration {
    private static final JsonNode REALM = JsonMapper.shared().readTree(Objects.requireNonNull(KeycloakTestConfiguration.class.getResourceAsStream("/keycloak.json")));

    private static final GenericContainer<?> CONTAINER = new GenericContainer<>("quay.io/keycloak/keycloak")
        .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
        .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "password")
        .withCopyFileToContainer(MountableFile.forClasspathResource("/keycloak.json"), "/opt/keycloak/data/import/keycloak.json")
        .withCommand("start-dev --import-realm")
        .withExposedPorts(8080)
        .waitingFor(Wait.forHttp("/realms/default").forPort(8080));

    @Bean
    public GenericContainer<?> keycloakContainer() {
        return CONTAINER;
    }

    @Bean
    public DynamicPropertyRegistrar keycloakProperties() {
        return registry -> {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KeycloakTestConfiguration::issuerUri);
            registry.add("spring.security.oauth2.client.provider.default.issuer-uri", KeycloakTestConfiguration::issuerUri);
            registry.add("spring.http.serviceclient.keycloak.base-url", KeycloakTestConfiguration::url);
        };
    }

    public static void reset() {
        final var token = token("master", Map.of("grant_type", "password", "client_id", "admin-cli", "username", "admin", "password", "password"));
        final var client = RestClient.builder().baseUrl(url() + "/admin/realms").defaultHeader("Authorization", "Bearer " + token).build();
        client.delete().uri("/default").retrieve().toBodilessEntity();
        client.post().body(REALM).retrieve().toBodilessEntity();
    }

    public static String serviceToken(final String clientId, final String clientSecret) {
        return token("default", Map.of("grant_type", "client_credentials", "client_id", clientId, "client_secret", clientSecret));
    }

    private static String token(final String realm, final Map<String, String> form) {
        return RestClient.create().post().uri(url() + "/realms/" + realm + "/protocol/openid-connect/token").contentType(MediaType.APPLICATION_FORM_URLENCODED).body(MultiValueMap.fromSingleValue(form)).retrieve().body(JsonNode.class).path("access_token").asString();
    }

    private static String url() {
        return "http://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(8080);
    }

    private static String issuerUri() {
        return url() + "/realms/default";
    }
}
