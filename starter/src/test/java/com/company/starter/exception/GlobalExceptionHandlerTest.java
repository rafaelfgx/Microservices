package com.company.starter.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

class GlobalExceptionHandlerTest {
    GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldHandleAsForbiddenWhenAccessDeniedExceptionOccurs() {
        assertions(globalExceptionHandler.handle(new AccessDeniedException("Message")), HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldHandleWithStatusWhenResponseStatusExceptionOccurs() {
        final var exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Message");
        assertions(globalExceptionHandler.handle(exception), HttpStatus.valueOf(exception.getStatusCode().value()));
    }

    @Test
    void shouldHandleWithStatusWhenResponseStatusExceptionOccursWithInternalServerError() {
        final var exception = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Message");
        assertions(globalExceptionHandler.handle(exception), HttpStatus.valueOf(exception.getStatusCode().value()));
    }

    @Test
    void shouldHandleAsGatewayTimeoutWhenSocketTimeoutExceptionOccurs() {
        assertions(globalExceptionHandler.handle(new SocketTimeoutException("Message")), HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    void shouldHandleAsNotFoundWhenNoResourceFoundExceptionOccurs() {
        assertions(globalExceptionHandler.handle(new NoResourceFoundException(HttpMethod.GET, "requestUri", "resourcePath")), HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldHandleAsBadGatewayWhenHttpServerErrorExceptionOccurs() {
        assertions(globalExceptionHandler.handle(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Message")), HttpStatus.BAD_GATEWAY);
    }

    @Test
    void shouldHandleAsBadGatewayWhenResourceAccessExceptionOccurs() {
        assertions(globalExceptionHandler.handle(new ResourceAccessException("Message")), HttpStatus.BAD_GATEWAY);
    }

    @Test
    void shouldHandleAsBadGatewayWhenUnknownHostExceptionOccurs() {
        assertions(globalExceptionHandler.handle(new UnknownHostException("Message")), HttpStatus.BAD_GATEWAY);
    }

    @Test
    void shouldHandleAsBadRequestWhenNumberFormatExceptionOccurs() {
        assertions(globalExceptionHandler.handleBadRequest(new NumberFormatException("Message")), HttpStatus.BAD_REQUEST, "/problems/request", "Request", "Invalid request");
    }

    @Test
    void shouldHandleAsBadRequestWhenDateTimeParseExceptionOccurs() {
        assertions(globalExceptionHandler.handleBadRequest(new DateTimeParseException("Message", "", 0)), HttpStatus.BAD_REQUEST, "/problems/request", "Request", "Invalid request");
    }

    @Test
    void shouldHandleAsBadRequestWhenHttpMessageNotReadableExceptionOccurs() {
        final var exception = new HttpMessageNotReadableException("Message", Mockito.mock());
        assertions(globalExceptionHandler.handle(exception), HttpStatus.BAD_REQUEST, "/problems/request", "Request", "Invalid request");
    }

    @Test
    void shouldHandleAsBadRequestWhenHttpMessageNotReadableExceptionContainsInvalidFormatException() {
        final var invalidFormatException = InvalidFormatException.from(null, "message", "abc", Long.class);
        invalidFormatException.prependPath(new Object(), "name");
        invalidFormatException.prependPath(new Object(), "user");
        final var exception = new HttpMessageNotReadableException("Message", invalidFormatException, Mockito.mock());
        assertions(globalExceptionHandler.handle(exception), List.of("user.name: must be valid"));
    }

    @Test
    void shouldHandleAsBadRequestWhenConstraintViolationExceptionOccurs() {
        final var path = Mockito.mock(Path.class);
        Mockito.when(path.toString()).thenReturn("field");
        final ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("must not be null");
        final var exception = new ConstraintViolationException(Set.of(violation));
        assertions(globalExceptionHandler.handle(exception), List.of("field: must not be null"));
    }

    @Test
    void shouldHandleAsBadRequestWhenMethodArgumentTypeMismatchExceptionOccurs() {
        final var exception = new MethodArgumentTypeMismatchException("abc", Long.class, "property", Mockito.mock(MethodParameter.class), new IllegalArgumentException());
        assertions(globalExceptionHandler.handle(exception), List.of("%s: must be valid".formatted(exception.getName())));
    }

    @Test
    void shouldHandleAsBadRequestWhenMethodArgumentNotValidExceptionOccurs() {
        final var bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getGlobalErrors()).thenReturn(List.of(new ObjectError("user", "must be active")));
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("user", "name", "must be valid")));
        final var exception = new MethodArgumentNotValidException(Mockito.mock(MethodParameter.class), bindingResult);
        assertions(globalExceptionHandler.handle(exception), List.of("user: must be active", "name: must be valid"));
    }

    @Test
    void shouldHandleAsBadRequestWhenMethodArgumentNotValidExceptionContainsTypeMismatchError() {
        final var bindingResult = Mockito.mock(BindingResult.class);
        final var fieldError = Mockito.mock(FieldError.class);
        Mockito.when(fieldError.getField()).thenReturn("age");
        Mockito.when(fieldError.getCode()).thenReturn("typeMismatch");
        Mockito.when(fieldError.getDefaultMessage()).thenReturn("must be a number");
        Mockito.when(bindingResult.getGlobalErrors()).thenReturn(List.of());
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        final var exception = new MethodArgumentNotValidException(Mockito.mock(MethodParameter.class), bindingResult);
        assertions(globalExceptionHandler.handle(exception), List.of("age: must be valid"));
    }

    @Test
    void shouldHandleAsNotFoundWhenApplicationExceptionOccurs() {
        final var exception = new ApplicationException(CustomerError.CUSTOMER_NOT_FOUND);
        assertions(globalExceptionHandler.handle(exception), HttpStatus.NOT_FOUND, "/problems/customer-not-found", "Customer Not Found", exception.getMessage());
    }

    @Test
    void shouldHandleAsConflictWhenApplicationExceptionOccursWithCause() {
        final var cause = new IllegalArgumentException("conflict");
        final var exception = new ApplicationException(CustomerError.CUSTOMER_EXISTS, cause);
        assertions(globalExceptionHandler.handle(exception), HttpStatus.CONFLICT, "/problems/customer-exists", "Customer Exists", exception.getMessage());
        Assertions.assertSame(cause, exception.getCause());
    }

    @Test
    void shouldHandleWithProblemDetailWhenErrorResponseExceptionOccurs() {
        final var exception = new ErrorResponseException(HttpStatus.CONFLICT, ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase()), null);
        final var response = globalExceptionHandler.handle(exception);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(exception.getBody().getStatus(), response.getBody().getStatus());
        Assertions.assertEquals(exception.getBody().getDetail(), response.getBody().getDetail());
    }

    @Test
    void shouldHandleAsInternalServerErrorWhenUnexpectedExceptionOccurs() {
        assertions(globalExceptionHandler.handle(new RuntimeException("Message")), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static void assertions(final ResponseEntity<Void> response, final HttpStatus status) {
        Assertions.assertNotNull(response);
        Assertions.assertEquals(status, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    private static void assertions(final ResponseEntity<ProblemDetail> response, final HttpStatus status, final String type, final String title, final String detail) {
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(status, response.getStatusCode());
        Assertions.assertEquals(status.value(), response.getBody().getStatus());
        Assertions.assertEquals(URI.create(type), response.getBody().getType());
        Assertions.assertEquals(title, response.getBody().getTitle());
        Assertions.assertEquals(detail, response.getBody().getDetail());
    }

    private static void assertions(final ResponseEntity<ProblemDetail> response, final List<String> errors) {
        assertions(response, HttpStatus.BAD_REQUEST, "/problems/request", "Request", "Invalid request");
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getProperties());
        Assertions.assertEquals(errors, response.getBody().getProperties().get("errors"));
    }
}
