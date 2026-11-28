package com.company.authservice.user;

import com.company.starter.validators.password.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.With;

import java.util.UUID;

public record ChangePasswordRequest(
    @Schema(hidden = true) @With UUID id,
    @NotBlank @Password String currentPassword,
    @NotBlank @Password String newPassword) {
}
