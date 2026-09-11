package com.company.starter.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handle(final AccessDeniedException ignoredException) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Void> handle(final ResponseStatusException exception) {
        if (exception.getStatusCode().is5xxServerError()) {
            log.error("[ResponseStatusException]", exception);
        }

        return ResponseEntity.status(exception.getStatusCode()).build();
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<Void> handle(final SocketTimeoutException exception) {
        log.error("[SocketTimeoutException]", exception);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handle(final NoResourceFoundException ignoredException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Void> handle(final HttpServerErrorException exception) {
        log.error("[HttpServerErrorException]", exception);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Void> handle(final ResourceAccessException exception) {
        log.error("[ResourceAccessException]", exception);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<Void> handle(final UnknownHostException exception) {
        log.error("[UnknownHostException]", exception);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    @ExceptionHandler({NumberFormatException.class, DateTimeParseException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(final Exception ignoredException) {
        return response(HttpStatus.BAD_REQUEST, "request", "Invalid request", List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handle(final HttpMessageNotReadableException exception) {
        if (!(exception.getMostSpecificCause() instanceof InvalidFormatException invalidFormatException)) {
            return response(HttpStatus.BAD_REQUEST, "request", "Invalid request", List.of());
        }

        final var path = invalidFormatException
            .getPath()
            .stream()
            .map(JacksonException.Reference::getPropertyName)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("."));

        final var errors = List.of("%s: must be valid".formatted(path));

        return response(HttpStatus.BAD_REQUEST, "request", "Invalid request", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handle(final ConstraintViolationException exception) {
        final var errors = exception
            .getConstraintViolations()
            .stream()
            .map(error -> "%s: %s".formatted(error.getPropertyPath(), error.getMessage()))
            .sorted()
            .toList();

        return response(HttpStatus.BAD_REQUEST, "request", "Invalid request", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handle(final MethodArgumentTypeMismatchException exception) {
        final var errors = List.of("%s: must be valid".formatted(exception.getName()));

        return response(HttpStatus.BAD_REQUEST, "request", "Invalid request", errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handle(final MethodArgumentNotValidException exception) {
        final var globals = exception
            .getGlobalErrors()
            .stream()
            .map(error -> "%s: %s".formatted(error.getObjectName(), error.getDefaultMessage()))
            .sorted();

        final var fields = exception
            .getFieldErrors()
            .stream()
            .map(error -> "%s: %s".formatted(error.getField(), "typeMismatch".equals(error.getCode()) ? "must be valid" : error.getDefaultMessage()))
            .sorted();

        return response(HttpStatus.BAD_REQUEST, "request", "Invalid request", Stream.concat(globals, fields).toList());
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ProblemDetail> handle(final ApplicationException exception) {
        return response(exception.getStatus(), exception.getType(), exception.getMessage(), List.of());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ProblemDetail> handle(final ErrorResponseException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getBody());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handle(final Exception exception) {
        log.error("[Exception]", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private ResponseEntity<ProblemDetail> response(final HttpStatusCode status, final String type, final String detail, final List<String> errors) {
        final var slug = type.replace(' ', '-').replace('_', '-').replace('.', '-').toLowerCase();
        final var title = Arrays.stream(slug.split("-")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
        final var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("/problems/" + slug));
        problemDetail.setTitle(title);
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
    }
}
