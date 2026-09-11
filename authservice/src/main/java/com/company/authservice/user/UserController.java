package com.company.authservice.user;

import com.company.authservice.auth.AuthService;
import com.company.starter.swagger.DefaultApiResponse;
import com.company.starter.swagger.PostApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users")
@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final AuthService authService;

    @Operation(summary = "Save")
    @PostApiResponse
    @PostMapping
    public ResponseEntity<UUID> save(@RequestBody @Valid final UserRequest request) {
        return authService.save(request);
    }

    @Operation(summary = "Delete")
    @DefaultApiResponse
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable final UUID id) {
        return authService.delete(id);
    }
}
