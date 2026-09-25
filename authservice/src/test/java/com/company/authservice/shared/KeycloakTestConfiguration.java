package com.company.authservice.shared;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
    public DynamicPropertyRegistrar keycloakProperties(final GenericContainer<?> keycloakContainer) {
        return registry -> {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KeycloakTestConfiguration::issuerUri);
            registry.add("spring.security.oauth2.client.provider.default.issuer-uri", KeycloakTestConfiguration::issuerUri);
            registry.add("spring.http.serviceclient.keycloak.base-url", KeycloakTestConfiguration::url);
        };
    }

    public static void reset() {
        final var form = MultiValueMap.fromSingleValue(Map.of("grant_type", "password", "client_id", "admin-cli", "username", "admin", "password", "password"));
        final var token = Objects.requireNonNull(RestClient.create().post().uri(url() + "/realms/master/protocol/openid-connect/token").body(form).retrieve().body(JsonNode.class)).path("access_token").asString();
        final var client = RestClient.builder().baseUrl(url() + "/admin/realms").defaultHeader("Authorization", "Bearer " + token).build();
        client.delete().uri("/default").retrieve().toBodilessEntity();
        client.post().body(REALM).retrieve().toBodilessEntity();
    }

    private static String url() {
        return "http://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(8080);
    }

    private static String issuerUri() {
        return url() + "/realms/default";
    }
}
