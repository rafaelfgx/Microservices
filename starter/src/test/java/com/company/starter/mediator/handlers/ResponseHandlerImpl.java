package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Response;
import com.company.starter.mediator.ResponseHandler;

public class ResponseHandlerImpl implements ResponseHandler<Response> {
    @Override
    public Response handle() {
        return new Response("ResponseHandler");
    }
}
