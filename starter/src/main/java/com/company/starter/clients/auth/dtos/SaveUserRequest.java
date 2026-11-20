package com.company.starter.clients.auth.dtos;

import com.company.starter.validators.password.Password;
import com.company.starter.validators.username.Username;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;

public record SaveUserRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank @Username String username,
    @NotBlank @Password String password) {

    public SaveUserRequest {
        email = StringUtils.lowerCase(email);
        username = StringUtils.lowerCase(username);
    }
}
