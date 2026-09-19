package com.company.starter.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    private final HttpStatus status;
    private final String type;

    public ApplicationException(final ApplicationError error) {
        super(error.getMessage());
        this.status = error.getStatus();
        this.type = error.toString();
    }

    public ApplicationException(final ApplicationError error, final Throwable cause) {
        super(error.getMessage(), cause);
        this.status = error.getStatus();
        this.type = error.toString();
    }
}
