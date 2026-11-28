package com.company.customerservice.shared;

import com.company.starter.exception.ApplicationError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Error implements ApplicationError {
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "Customer not found");

    private final HttpStatus status;
    private final String message;
}
