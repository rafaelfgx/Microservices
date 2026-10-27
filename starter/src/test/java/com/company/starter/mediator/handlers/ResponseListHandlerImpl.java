package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Response;
import com.company.starter.mediator.ResponseHandler;

import java.util.List;

public class ResponseListHandlerImpl implements ResponseHandler<List<Response>> {
    @Override
    public List<Response> handle() {
        return List.of(new Response("ResponseListHandler"));
    }
}
