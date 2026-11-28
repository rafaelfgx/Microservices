package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Request;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.mediator.Response;

public class RequestResponseHandlerImpl implements RequestResponseHandler<Request, Response> {
    @Override
    public Response handle(final Request request) {
        return new Response("RequestResponseHandler");
    }
}
