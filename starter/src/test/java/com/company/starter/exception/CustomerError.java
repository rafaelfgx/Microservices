package com.company.starter.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomerError implements ApplicationError {
    CUSTOMER_EXISTS(HttpStatus.CONFLICT, "Customer already exists"),
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "Customer not found");

    private final HttpStatus status;
    private final String message;
}
