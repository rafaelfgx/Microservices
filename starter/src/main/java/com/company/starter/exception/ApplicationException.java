package com.company.starter.exception;

import org.springframework.web.ErrorResponseException;

public class ApplicationException extends ErrorResponseException {
    public ApplicationException(final ApplicationError error) {
        this(error, null);
    }

    public ApplicationException(final ApplicationError error, final Throwable cause) {
        super(error.getStatus(), cause);
        setDetail(error.getMessage());
    }
}