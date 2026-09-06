package com.company.authservice.auth;

import com.company.authservice.user.UserRequest;
import com.company.starter.exception.ApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthProperties properties;

    public ResponseEntity<AuthResponse> auth(final AuthRequest request) {
        try (final var keycloak = keycloakPassword(request.username(), request.password())) {
            return ResponseEntity.ok(toAuthResponse(keycloak.tokenManager().getAccessToken()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    public ResponseEntity<UUID> save(final UserRequest request) {
        final var users = list(request.username(), request.email());

        return users.isEmpty()
            ? create(request)
            : update(get(users, request.username(), request.email()), request);
    }

    public ResponseEntity<Void> delete(final UUID userId) {
        return execute(realm -> {
            final var response = realm.users().delete(userId.toString());

            try (response) {
                return response.getStatus() == HttpStatus.NOT_FOUND.value()
                    ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                    : ResponseEntity.status(response.getStatus()).build();
            }
        });
    }

    private Collection<UserRepresentation> list(final String username, final String email) {
        return execute(realm -> Stream
            .concat(realm.users().searchByUsername(username, true).stream(), realm.users().searchByEmail(email, true).stream())
            .collect(Collectors.toMap(UserRepresentation::getId, Function.identity(), (user, _) -> user))
            .values());
    }

    private UserRepresentation get(final Collection<UserRepresentation> users, final String username, final String email) {
        return users
            .stream()
            .filter(match -> username.equalsIgnoreCase(match.getUsername()) && email.equalsIgnoreCase(match.getEmail()))
            .findFirst()
            .orElseThrow(() -> new ApplicationException(AuthError.USER_EXISTS));
    }

    private ResponseEntity<UUID> create(final UserRequest request) {
        return execute(realm -> {
            final var response = realm.users().create(toUserRepresentation(request));

            try (response) {
                return response.getStatus() == HttpStatus.CREATED.value()
                    ? ResponseEntity.status(response.getStatus()).body(getId(response))
                    : ResponseEntity.status(response.getStatus()).build();
            }
        });
    }

    private ResponseEntity<UUID> update(final UserRepresentation user, final UserRequest request) {
        return execute(realm -> {
            user.setFirstName(request.name());
            user.setLastName(request.name());
            final var resource = realm.users().get(user.getId());
            resource.update(user);
            resource.resetPassword(toCredentialRepresentation(request.password()));
            return ResponseEntity.ok(UUID.fromString(user.getId()));
        });
    }

    private static UUID getId(final Response response) {
        return UUID.fromString(response.getLocation().getPath().replaceAll(".*/", ""));
    }

    private static AuthResponse toAuthResponse(final AccessTokenResponse response) {
        return new AuthResponse(
            response.getToken(),
            response.getExpiresIn(),
            response.getRefreshToken(),
            response.getRefreshExpiresIn()
        );
    }

    private static CredentialRepresentation toCredentialRepresentation(final String password) {
        final var credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);
        return credential;
    }

    private static UserRepresentation toUserRepresentation(final UserRequest request) {
        final var user = new UserRepresentation();
        user.setFirstName(request.name());
        user.setLastName(request.name());
        user.setUsername(request.username().toLowerCase());
        user.setCredentials(List.of(toCredentialRepresentation(request.password())));
        user.setEmail(request.email().toLowerCase());
        user.setEmailVerified(true);
        user.setEnabled(true);
        return user;
    }

    private <T> T execute(final Function<RealmResource, T> action) {
        try (final var keycloak = keycloakClientCredentials()) {
            return action.apply(keycloak.realm(properties.getRealm()));
        }
    }

    private KeycloakBuilder keycloakBuilder() {
        return KeycloakBuilder
            .builder()
            .serverUrl(properties.getUrl())
            .realm(properties.getRealm())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret());
    }

    private Keycloak keycloakClientCredentials() {
        return keycloakBuilder().grantType(OAuth2Constants.CLIENT_CREDENTIALS).build();
    }

    private Keycloak keycloakPassword(final String username, final String password) {
        return keycloakBuilder().grantType(OAuth2Constants.PASSWORD).username(username).password(password).build();
    }
}
