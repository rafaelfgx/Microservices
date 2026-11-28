package com.company.authservice.keycloak;

import com.company.authservice.auth.AuthRequest;
import com.company.authservice.auth.AuthResponse;
import com.company.authservice.shared.Error;
import com.company.authservice.user.ChangePasswordRequest;
import com.company.authservice.user.SaveUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class KeycloakService {
    private final KeycloakAdminClient keycloakAdminClient;
    private final KeycloakTokenClient keycloakTokenClient;
    private final KeycloakProperties keycloakProperties;
    private final JwtDecoder jwtDecoder;

    public AuthResponse auth(final AuthRequest request) {
        try {
            final var form = new LinkedMultiValueMap<String, String>();
            form.add("grant_type", "password");
            form.add("client_id", keycloakProperties.clientId());
            form.add("client_secret", keycloakProperties.clientSecret());
            form.add("username", request.username());
            form.add("password", request.password());
            final var token = keycloakTokenClient.token(form);
            return new AuthResponse(token.accessToken(), token.expiresIn(), token.refreshToken(), token.refreshExpiresIn());
        } catch (final HttpClientErrorException.BadRequest | HttpClientErrorException.Unauthorized ignored) {
            throw Error.INVALID_CREDENTIALS.exception();
        }
    }

    public UUID save(final SaveUserRequest request) {
        try {
            return create(request);
        } catch (final HttpClientErrorException.Conflict ignored) {
            return update(request);
        }
    }

    public void verify(final UUID id) {
        keycloakAdminClient.update(id, KeycloakUser.builder().emailVerified(true).build());
    }

    public void delete(final UUID id) {
        keycloakAdminClient.delete(id);
    }

    public void changePassword(final ChangePasswordRequest request) {
        verifyPassword(request.id(), request.currentPassword());
        keycloakAdminClient.resetPassword(request.id(), KeycloakCredential.password(request.newPassword()));
    }

    private UUID create(final SaveUserRequest request) {
        final var response = keycloakAdminClient.create(user(request));
        return UUID.fromString(StringUtils.getFilename(response.getHeaders().getLocation().getPath()));
    }

    private UUID update(final SaveUserRequest request) {
        final var users = Stream.concat(keycloakAdminClient.searchByUsername(request.username()).stream(), keycloakAdminClient.searchByEmail(request.email()).stream()).distinct().toList();
        if (users.size() != 1 || isVerified(users.getFirst())) throw Error.USER_ALREADY_EXISTS.exception();
        final var id = users.getFirst().id();
        keycloakAdminClient.update(id, user(request));
        return id;
    }

    private static boolean isVerified(final KeycloakUser user) {
        return user.email() == null || user.emailVerified();
    }

    private void verifyPassword(final UUID id, final String password) {
        final var token = auth(new AuthRequest(keycloakAdminClient.get(id).username(), password));

        if (!Objects.equals(id.toString(), jwtDecoder.decode(token.accessToken()).getSubject())) {
            throw Error.INVALID_CREDENTIALS.exception();
        }
    }

    private static KeycloakUser user(final SaveUserRequest request) {
        return KeycloakUser
            .builder()
            .firstName(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString())
            .username(request.username())
            .email(request.email())
            .emailVerified(false)
            .credentials(List.of(KeycloakCredential.password(request.password())))
            .enabled(true)
            .build();
    }
}
