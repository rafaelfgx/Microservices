package com.company.authservice.shared;

import com.company.authservice.auth.AuthRequest;
import com.company.authservice.keycloak.KeycloakCredential;
import com.company.authservice.keycloak.KeycloakProperties;
import com.company.authservice.keycloak.KeycloakUser;
import com.company.authservice.user.SaveUserRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Data {
    public static final UUID ID = UUID.randomUUID();
    public static final AuthRequest AUTH_REQUEST_VALID = new AuthRequest("admin", "P@$$w0rd");
    public static final AuthRequest AUTH_REQUEST_INVALID = new AuthRequest("invalid", "invalid");
    public static final SaveUserRequest SAVE_USER_REQUEST = new SaveUserRequest("Name", "email@mail.com", "username", "password");
    public static final KeycloakProperties KEYCLOAK_PROPERTIES = new KeycloakProperties("client", "secret");
    public static final KeycloakUser KEYCLOAK_USER = KeycloakUser
        .builder()
        .username(SAVE_USER_REQUEST.username())
        .email(SAVE_USER_REQUEST.email())
        .firstName(SAVE_USER_REQUEST.name())
        .lastName(SAVE_USER_REQUEST.name())
        .enabled(true)
        .emailVerified(true)
        .credentials(List.of(KeycloakCredential.password(SAVE_USER_REQUEST.password())))
        .build();
}
