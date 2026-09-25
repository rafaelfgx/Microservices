package com.company.starter.mediator;

import jakarta.validation.Valid;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@Component
public class Mediator {
    private final Map<Class<?>, BaseHandler> handlers;

    public Mediator(final ObjectProvider<BaseHandler> handlers) {
        this.handlers = handlers.orderedStream().collect(Collectors.toUnmodifiableMap(AopUtils::getTargetClass, Function.identity()));
    }

    public <H extends Handler> void handle(final Class<H> handler) {
        handler.cast(handlers.get(handler)).handle();
    }

    public <H extends RequestHandler<Request>, Request extends Record> void handleRequest(final Class<H> handler, @Valid final Request request) {
        handler.cast(handlers.get(handler)).handle(request);
    }

    public <H extends ResponseHandler<Response>, Response> Response handleResponse(final Class<H> handler) {
        return handler.cast(handlers.get(handler)).handle();
    }

    public <H extends RequestResponseHandler<Request, Response>, Request extends Record, Response> Response handle(final Class<H> handler, @Valid final Request request) {
        return handler.cast(handlers.get(handler)).handle(request);
    }
}
