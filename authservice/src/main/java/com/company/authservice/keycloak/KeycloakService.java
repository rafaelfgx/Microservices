package com.company.authservice.keycloak;

import com.company.authservice.auth.AuthError;
import com.company.authservice.auth.AuthRequest;
import com.company.authservice.auth.AuthResponse;
import com.company.authservice.user.SaveUserRequest;
import com.company.starter.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class KeycloakService {
    private final KeycloakProperties properties;
    private final KeycloakAdminClient adminClient;
    private final KeycloakTokenClient tokenClient;

    public AuthResponse auth(final AuthRequest request) {
        try {
            final var token = tokenClient.token(passwordGrant(request));
            return new AuthResponse(token.accessToken(), token.expiresIn(), token.refreshToken(), token.refreshExpiresIn());
        } catch (final HttpClientErrorException.BadRequest | HttpClientErrorException.Unauthorized ignored) {
            throw new ApplicationException(AuthError.INVALID_CREDENTIALS);
        }
    }

    public UUID save(final SaveUserRequest request) {
        try {
            final var location = Objects.requireNonNull(adminClient.create(user(request)).getHeaders().getLocation());
            return UUID.fromString(StringUtils.getFilename(location.getPath()));
        } catch (final HttpClientErrorException.Conflict ignored) {
            return existingId(request);
        }
    }

    public void delete(final UUID id) {
        adminClient.delete(id);
    }

    private UUID existingId(final SaveUserRequest request) {
        return adminClient
            .search(request.username(), request.email())
            .stream()
            .findFirst()
            .map(KeycloakUser::id)
            .orElseThrow(() -> new ApplicationException(AuthError.USER_EXISTS));
    }

    private MultiValueMap<String, String> passwordGrant(final AuthRequest request) {
        return MultiValueMap.fromSingleValue(Map.of(
            "grant_type", "password",
            "client_id", properties.clientId(),
            "client_secret", properties.clientSecret(),
            "username", request.username(),
            "password", request.password()
        ));
    }

    private static KeycloakUser user(final SaveUserRequest request) {
        return KeycloakUser
            .builder()
            .username(request.username())
            .email(request.email())
            .firstName(request.name())
            .lastName(request.name())
            .enabled(true)
            .emailVerified(true)
            .credentials(List.of(KeycloakCredential.password(request.password())))
            .build();
    }
}
