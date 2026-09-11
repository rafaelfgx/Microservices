package com.company.starter.exception;

import org.springframework.http.HttpStatus;

public interface ApplicationError {
    HttpStatus getStatus();

    String getMessage();
}
