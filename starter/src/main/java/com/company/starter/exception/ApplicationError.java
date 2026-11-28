package com.company.starter.exception;

import org.springframework.http.HttpStatus;

public interface ApplicationError {
    HttpStatus getStatus();

    String getMessage();

    default RuntimeException exception() {
        return new ApplicationException(this);
    }

    default RuntimeException exception(final Throwable cause) {
        return new ApplicationException(this, cause);
    }
}
