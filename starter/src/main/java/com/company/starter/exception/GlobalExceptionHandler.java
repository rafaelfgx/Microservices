package com.company.starter.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@NullMarked
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public @Nullable ResponseEntity<Object> handle(final AccessDeniedException exception, final WebRequest request) {
        return handleExceptionInternal(exception, null, HttpHeaders.EMPTY, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public @Nullable ResponseEntity<Object> handle(final ConstraintViolationException exception, final WebRequest request) {
        final var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        return handleExceptionInternal(exception, body, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ProblemDetail> handle(final HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getResponseBodyAs(ProblemDetail.class));
    }

    @ExceptionHandler({HttpServerErrorException.class, ResourceAccessException.class})
    public @Nullable ResponseEntity<Object> handleBadGateway(final Exception exception, final WebRequest request) {
        return handleExceptionInternal(exception, null, HttpHeaders.EMPTY, HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(Exception.class)
    public @Nullable ResponseEntity<Object> handle(final Exception exception, final WebRequest request) {
        return handleExceptionInternal(exception, null, HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(final Exception exception, final @Nullable Object body, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
        if (status.is5xxServerError()) log.error("[{}]", exception.getClass().getSimpleName(), exception);
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }
}
