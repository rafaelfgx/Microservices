package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Request;
import com.company.starter.mediator.RequestHandler;

public class RequestHandlerImpl implements RequestHandler<Request> {
    @Override
    public void handle(Request request) {
        System.out.println();
    }
}
