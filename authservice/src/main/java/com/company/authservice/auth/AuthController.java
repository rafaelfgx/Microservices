package com.company.authservice.auth;

import com.company.starter.swagger.DefaultApiResponse;
import com.company.starter.swagger.GetApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final AuthService authService;

    @Operation(summary = "Auth")
    @DefaultApiResponse
    @PostMapping
    public ResponseEntity<AuthResponse> auth(@RequestBody @Valid final AuthRequest request) {
        return authService.auth(request);
    }

    @Operation(summary = "Get")
    @GetApiResponse
    @GetMapping
    public ResponseEntity<Jwt> get(@AuthenticationPrincipal @Valid final Jwt jwt) {
        return ResponseEntity.ok(jwt);
    }
}
