package com.company.starter.mediator;

@FunctionalInterface
public non-sealed interface RequestHandler<Request> extends BaseHandler {
    void handle(final Request request);
}
