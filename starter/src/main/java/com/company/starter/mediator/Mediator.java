package com.company.starter.mediator;

import com.company.starter.validation.ValidationService;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Mediator {
    private final ValidationService validationService;
    private final Map<Class<?>, BaseHandler> handlers;

    public Mediator(final ValidationService validationService, final List<BaseHandler> handlers) {
        this.validationService = validationService;
        this.handlers = handlers.stream().collect(Collectors.toMap(AopUtils::getTargetClass, Function.identity()));
    }

    public <H extends Handler> void handle(final Class<H> handler) {
        handler.cast(handlers.get(handler)).handle();
    }

    public <H extends RequestHandler<Request>, Request> void handleRequest(final Class<H> handler, final Request request) {
        validationService.validateOrThrow(request);
        handler.cast(handlers.get(handler)).handle(request);
    }

    public <H extends ResponseHandler<Response>, Response> Response handleResponse(final Class<H> handler) {
        return handler.cast(handlers.get(handler)).handle();
    }

    public <H extends RequestResponseHandler<Request, Response>, Request, Response> Response handle(final Class<H> handler, final Request request) {
        validationService.validateOrThrow(request);
        return handler.cast(handlers.get(handler)).handle(request);
    }
}
