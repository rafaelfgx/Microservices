package com.company.authservice.shared;

import com.company.authservice.auth.AuthRequest;
import com.company.authservice.user.UserRequest;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Data {
    public static final AuthRequest AUTH_REQUEST_VALID = new AuthRequest("admin", "P@$$w0rd");
    public static final AuthRequest AUTH_REQUEST_INVALID = new AuthRequest("invalid", "invalid");
    public static final UserRequest USER_REQUEST = new UserRequest("Name", "email@mail.com", "username", "password");
}
