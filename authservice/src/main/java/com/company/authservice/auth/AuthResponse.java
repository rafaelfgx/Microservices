package com.company.authservice.auth;

public record AuthResponse(String accessToken, Long accessTokenExpiresIn, String refreshToken, Long refreshTokenExpiresIn) {
}
