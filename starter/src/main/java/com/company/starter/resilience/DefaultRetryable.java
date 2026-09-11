package com.company.starter.resilience;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoConnectionPoolClearedException;
import com.mongodb.MongoNodeIsRecoveringException;
import com.mongodb.MongoNotPrimaryException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;
import jakarta.validation.ValidationException;
import org.springframework.core.annotation.AliasFor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.FileNotFoundException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    includes = {
        ConnectException.class,
        DataAccessResourceFailureException.class,
        HttpServerErrorException.BadGateway.class,
        HttpServerErrorException.GatewayTimeout.class,
        HttpServerErrorException.InternalServerError.class,
        HttpServerErrorException.ServiceUnavailable.class,
        MongoConnectionPoolClearedException.class,
        MongoNodeIsRecoveringException.class,
        MongoNotPrimaryException.class,
        MongoSocketException.class,
        MongoSocketReadTimeoutException.class,
        MongoTimeoutException.class,
        NoRouteToHostException.class,
        OptimisticLockingFailureException.class,
        PortUnreachableException.class,
        QueryTimeoutException.class,
        RejectedExecutionException.class,
        ResourceAccessException.class,
        SocketException.class,
        SocketTimeoutException.class,
        TimeoutException.class,
        TransientDataAccessException.class,
        UnknownHostException.class
    },
    excludes = {
        AccessDeniedException.class,
        BadCredentialsException.class,
        DataIntegrityViolationException.class,
        DuplicateKeyException.class,
        FileNotFoundException.class,
        HttpClientErrorException.BadRequest.class,
        HttpClientErrorException.Forbidden.class,
        HttpClientErrorException.MethodNotAllowed.class,
        HttpClientErrorException.NotFound.class,
        HttpClientErrorException.Unauthorized.class,
        HttpMessageNotReadableException.class,
        IllegalArgumentException.class,
        IllegalMonitorStateException.class,
        IllegalStateException.class,
        IndexOutOfBoundsException.class,
        InvalidDataAccessApiUsageException.class,
        MethodArgumentNotValidException.class,
        NullPointerException.class,
        NumberFormatException.class,
        UnsupportedOperationException.class,
        ValidationException.class
    },
    maxRetriesString = "${resilience.retry.max-retries}",
    delayString = "${resilience.retry.delay}",
    jitterString = "${resilience.retry.jitter}",
    multiplierString = "${resilience.retry.multiplier}",
    maxDelayString = "${resilience.retry.max-delay}",
    timeUnit = java.util.concurrent.TimeUnit.MILLISECONDS
)
public @interface DefaultRetryable {
    @AliasFor(annotation = Retryable.class, attribute = "maxRetriesString")
    String maxRetries() default "${resilience.retry.max-retries}";

    @AliasFor(annotation = Retryable.class, attribute = "delayString")
    String delay() default "${resilience.retry.delay}";

    @AliasFor(annotation = Retryable.class, attribute = "jitterString")
    String jitter() default "${resilience.retry.jitter}";

    @AliasFor(annotation = Retryable.class, attribute = "multiplierString")
    String multiplier() default "${resilience.retry.multiplier}";

    @AliasFor(annotation = Retryable.class, attribute = "maxDelayString")
    String maxDelay() default "${resilience.retry.max-delay}";
}
