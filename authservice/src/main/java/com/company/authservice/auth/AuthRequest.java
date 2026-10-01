package com.company.authservice.auth;

import com.company.starter.validators.password.Password;
import com.company.starter.validators.username.Username;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank @Username String username, @NotBlank @Password String password) {
}
