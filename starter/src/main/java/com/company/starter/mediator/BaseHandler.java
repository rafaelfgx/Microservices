package com.company.starter.mediator;

public sealed interface BaseHandler permits Handler, RequestHandler, ResponseHandler, RequestResponseHandler {
}
