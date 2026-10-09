package com.company.starter.mediator;

@FunctionalInterface
public non-sealed interface RequestResponseHandler<Request, Response> extends BaseHandler {
    Response handle(final Request request);
}
