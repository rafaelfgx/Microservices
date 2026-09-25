package com.company.starter.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;

class GlobalExceptionHandlerTest {
    GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    @Test
    void shouldHandleAsForbiddenWhenAccessDeniedExceptionOccurs() {
        assertionsWithoutBody(globalExceptionHandler.handle(new AccessDeniedException("Message"), request), HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldHandleWithProblemDetailWhenResponseStatusExceptionOccurs() throws Exception {
        assertions(globalExceptionHandler.handleException(new ResponseStatusException(HttpStatus.NOT_FOUND, "Message"), request), HttpStatus.NOT_FOUND, "Message");
    }

    @Test
    void shouldHandleAsNotFoundWhenNoResourceFoundExceptionOccurs() throws Exception {
        assertions(globalExceptionHandler.handleException(new NoResourceFoundException(HttpMethod.GET, "requestUri", "resourcePath"), request), HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldHandleAsMethodNotAllowedWhenHttpRequestMethodNotSupportedExceptionOccurs() throws Exception {
        assertions(globalExceptionHandler.handleException(new HttpRequestMethodNotSupportedException("PATCH"), request), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void shouldHandleAsUnsupportedMediaTypeWhenHttpMediaTypeNotSupportedExceptionOccurs() throws Exception {
        assertions(globalExceptionHandler.handleException(new HttpMediaTypeNotSupportedException("Message"), request), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void shouldHandleAsBadRequestWhenMissingServletRequestParameterExceptionOccurs() throws Exception {
        assertions(globalExceptionHandler.handleException(new MissingServletRequestParameterException("name", "String"), request), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldHandleAsBadRequestWhenHandlerMethodValidationExceptionOccurs() throws Exception {
        final var validationResult = Mockito.mock(MethodValidationResult.class);
        assertions(globalExceptionHandler.handleException(new HandlerMethodValidationException(validationResult), request), HttpStatus.BAD_REQUEST, "Validation failure");
    }

    @Test
    void shouldHandleWithProblemDetailWhenHttpClientErrorExceptionOccurs() {
        final var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Message");
        final var exception = new HttpClientErrorException(HttpStatus.CONFLICT, "Conflict", null, null, null);
        exception.setBodyConvertFunction(_ -> problemDetail);
        final var response = globalExceptionHandler.handle(exception);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertSame(problemDetail, response.getBody());
    }

    @Test
    void shouldHandleAsBadGatewayWhenHttpServerErrorExceptionOccurs() {
        assertionsWithoutBody(globalExceptionHandler.handleBadGateway(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Message"), request), HttpStatus.BAD_GATEWAY);
    }

    @Test
    void shouldHandleAsBadGatewayWhenResourceAccessExceptionOccurs() {
        assertionsWithoutBody(globalExceptionHandler.handleBadGateway(new ResourceAccessException("Message"), request), HttpStatus.BAD_GATEWAY);
    }

    @Test
    void shouldHandleAsBadRequestWhenHttpMessageNotReadableExceptionOccurs() throws Exception {
        final var exception = new HttpMessageNotReadableException("Message", Mockito.mock());
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.BAD_REQUEST, "Failed to read request");
    }

    @Test
    void shouldHandleAsBadRequestWhenConstraintViolationExceptionOccurs() {
        final var path = Mockito.mock(Path.class);
        Mockito.when(path.toString()).thenReturn("field");
        final ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("must not be null");
        final var exception = new ConstraintViolationException(Set.of(violation));
        assertions(globalExceptionHandler.handle(exception, request), HttpStatus.BAD_REQUEST, "field: must not be null");
    }

    @Test
    void shouldHandleAsBadRequestWhenMethodArgumentTypeMismatchExceptionOccurs() throws Exception {
        final var exception = new MethodArgumentTypeMismatchException("abc", Long.class, "age", Mockito.mock(MethodParameter.class), new IllegalArgumentException());
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.BAD_REQUEST, "Failed to convert 'age' with value: 'abc'");
    }

    @Test
    void shouldHandleAsBadRequestWhenTypeMismatchExceptionOccurs() throws Exception {
        final var exception = new TypeMismatchException(new PropertyChangeEvent(new Object(), "age", null, "abc"), Long.class);
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.BAD_REQUEST, "Failed to convert 'age' with value: 'abc'");
    }

    @Test
    void shouldHandleAsServiceUnavailableWhenAsyncRequestTimeoutExceptionOccurs() throws Exception {
        assertions(globalExceptionHandler.handleException(new AsyncRequestTimeoutException(), request), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldHandleAsBadRequestWhenMethodArgumentNotValidExceptionOccurs() throws Exception {
        final var bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("user", "name", "must be valid")));
        final var exception = new MethodArgumentNotValidException(Mockito.mock(MethodParameter.class), bindingResult);
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.BAD_REQUEST, "Invalid request content.");
    }

    @Test
    void shouldResolveFieldErrorsWhenMessageSourceIsConfigured() throws Exception {
        final var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("starter");
        globalExceptionHandler.setMessageSource(messageSource);
        final var bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError("name", "NotBlank"), fieldError("email", "Email")));
        final var exception = new MethodArgumentNotValidException(Mockito.mock(MethodParameter.class), bindingResult);
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.BAD_REQUEST, "Invalid request: name: must not be blank, and email: must be a valid email address");
    }

    @Test
    void shouldHandleAsNotFoundWhenApplicationExceptionOccurs() throws Exception {
        final var exception = new ApplicationException(CustomerError.CUSTOMER_NOT_FOUND);
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.NOT_FOUND, CustomerError.CUSTOMER_NOT_FOUND.getMessage());
    }

    @Test
    void shouldHandleAsConflictWhenApplicationExceptionOccursWithCause() throws Exception {
        final var cause = new IllegalArgumentException("conflict");
        final var exception = new ApplicationException(CustomerError.CUSTOMER_EXISTS, cause);
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.CONFLICT, CustomerError.CUSTOMER_EXISTS.getMessage());
        Assertions.assertSame(cause, exception.getCause());
    }

    @Test
    void shouldHandleWithProblemDetailWhenErrorResponseExceptionOccurs() throws Exception {
        final var exception = new ErrorResponseException(HttpStatus.CONFLICT, ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Message"), null);
        assertions(globalExceptionHandler.handleException(exception, request), HttpStatus.CONFLICT, "Message");
    }

    @Test
    void shouldHandleAsInternalServerErrorWhenUnexpectedExceptionOccurs() {
        assertionsWithoutBody(globalExceptionHandler.handle(new RuntimeException("Message"), request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static FieldError fieldError(final String field, final String code) {
        final var argument = new DefaultMessageSourceResolvable(new String[]{"user." + field, field}, field);
        return new FieldError("user", field, null, false, new String[]{code + ".user." + field, code + "." + field, code}, new Object[]{argument}, null);
    }

    private static ProblemDetail problemDetail(final ResponseEntity<?> response) {
        return Assertions.assertInstanceOf(ProblemDetail.class, response.getBody());
    }

    private static void assertionsWithoutBody(final ResponseEntity<?> response, final HttpStatus status) {
        Assertions.assertNotNull(response);
        Assertions.assertEquals(status, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    private static void assertions(final ResponseEntity<?> response, final HttpStatus status) {
        Assertions.assertNotNull(response);
        Assertions.assertEquals(status, response.getStatusCode());
        Assertions.assertEquals(status.value(), problemDetail(response).getStatus());
    }

    private static void assertions(final ResponseEntity<?> response, final HttpStatus status, final String detail) {
        assertions(response, status);
        Assertions.assertEquals(detail, problemDetail(response).getDetail());
    }
}
