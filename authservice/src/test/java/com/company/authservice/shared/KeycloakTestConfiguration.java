package com.company.authservice.shared;

import lombok.SneakyThrows;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.util.JsonSerialization;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Paths;

@TestConfiguration
public class KeycloakTestConfiguration {
    private static final String IMAGE = "quay.io/keycloak/keycloak";
    private static final String CLIENT = "admin-cli";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String REALM = "microservices";
    private static final String REALM_PATH = "src/test/resources/realm.json";
    private static final String REALM_PATH_CONTAINER = "/opt/keycloak/data/import/realm.json";
    private static final int PORT = 8080;
    private static final int HEALTH_PORT = 9000;

    private static final GenericContainer<?> CONTAINER = new GenericContainer<>(DockerImageName.parse(IMAGE))
        .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", USERNAME)
        .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", PASSWORD)
        .withEnv("KC_HEALTH_ENABLED", "true")
        .withEnv("KC_HOSTNAME_STRICT", "false")
        .withEnv("KC_HOSTNAME_STRICT_HTTPS", "false")
        .withFileSystemBind(REALM_PATH, REALM_PATH_CONTAINER, BindMode.READ_ONLY)
        .withCommand("start-dev --import-realm --verbose")
        .withExposedPorts(PORT, HEALTH_PORT)
        .waitingFor(Wait.forHttp("/health/started").forPort(HEALTH_PORT));

    @Bean
    public GenericContainer<?> keycloakContainer() {
        return CONTAINER;
    }

    @Bean
    public DynamicPropertyRegistrar keycloakProperties(final GenericContainer<?> keycloakContainer) {
        return registry -> {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KeycloakTestConfiguration::issuerUri);
            registry.add("spring.security.oauth2.client.provider.default.issuer-uri", KeycloakTestConfiguration::issuerUri);
            registry.add("auth.url", KeycloakTestConfiguration::url);
        };
    }

    @SneakyThrows
    public static void reset() {
        try (final var keycloak = keycloak()) {
            keycloak.realms().realm(REALM).remove();
            keycloak.realms().create(realm());
        }
    }

    private static String url() {
        return "http://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(PORT);
    }

    private static String issuerUri() {
        return url() + "/realms/" + REALM;
    }

    private static Keycloak keycloak() {
        return KeycloakBuilder.builder().serverUrl(url()).realm("master").clientId(CLIENT).username(USERNAME).password(PASSWORD).build();
    }

    @SneakyThrows
    private static RealmRepresentation realm() {
        return JsonSerialization.readValue(Files.readString(Paths.get(REALM_PATH)), RealmRepresentation.class);
    }
}
