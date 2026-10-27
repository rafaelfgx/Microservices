package com.company.authservice.auth;

import com.company.authservice.keycloak.KeycloakService;
import com.company.starter.swagger.BaseApiResponse;
import com.company.starter.swagger.PostApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final KeycloakService keycloakService;

    @Operation(summary = "Auth")
    @PostApiResponse
    @PostMapping
    public AuthResponse auth(@RequestBody @Valid final AuthRequest request) {
        return keycloakService.auth(request);
    }

    @Operation(summary = "Get")
    @BaseApiResponse
    @GetMapping
    public Jwt get(@AuthenticationPrincipal final Jwt jwt) {
        return jwt;
    }
}
