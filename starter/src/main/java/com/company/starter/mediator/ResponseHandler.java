package com.company.starter.mediator;

@FunctionalInterface
public non-sealed interface ResponseHandler<Response> extends BaseHandler {
    Response handle();
}
