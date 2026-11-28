package com.company.authservice.user;

import com.company.authservice.keycloak.KeycloakService;
import com.company.starter.swagger.BaseApiResponse;
import com.company.starter.swagger.PostApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users")
@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final KeycloakService keycloakService;

    @Operation(summary = "Save")
    @ResponseStatus(HttpStatus.OK)
    @PostApiResponse
    @PostMapping
    public UUID save(@RequestBody @Valid final SaveUserRequest request) {
        return keycloakService.save(request);
    }

    @Operation(summary = "Verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @BaseApiResponse
    @PatchMapping("{id}/verify")
    public void verify(@PathVariable final UUID id) {
        keycloakService.verify(id);
    }

    @Operation(summary = "Delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @BaseApiResponse
    @DeleteMapping("{id}")
    public void delete(@PathVariable final UUID id) {
        keycloakService.delete(id);
    }

    @Operation(summary = "Change password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @BaseApiResponse
    @PatchMapping("/change/password")
    public void changePassword(@AuthenticationPrincipal final Jwt jwt, @RequestBody @Valid final ChangePasswordRequest request) {
        keycloakService.changePassword(request.withId(UUID.fromString(jwt.getSubject())));
    }
}
