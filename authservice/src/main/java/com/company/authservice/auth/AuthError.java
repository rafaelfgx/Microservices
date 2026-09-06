package com.company.authservice.auth;

import com.company.starter.exception.ApplicationError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthError implements ApplicationError {
    USER_EXISTS(HttpStatus.CONFLICT, "User already exists");

    private final HttpStatus status;
    private final String message;
}
