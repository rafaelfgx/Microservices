package com.company.customerservice.customer;

import com.company.starter.exception.ApplicationError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomerError implements ApplicationError {
    CUSTOMER_EXISTS(HttpStatus.CONFLICT, "Customer already exists");

    private final HttpStatus status;
    private final String message;
}
