package com.company.authservice.auth;

import com.company.starter.validation.password.Password;
import com.company.starter.validation.username.Username;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank @Username String username,
    @NotBlank @Password String password) {
}
