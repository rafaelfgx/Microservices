package com.company.authservice.shared;

import com.company.starter.exception.ApplicationError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error implements ApplicationError {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists");

    private final HttpStatus status;
    private final String message;
}
