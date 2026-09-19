package com.company.authservice.auth;

public record AuthResponse(
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    long refreshTokenExpiresIn) {
}
