package com.company.starter.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error implements ApplicationError {
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found");

    private final HttpStatus status;
    private final String message;
}
