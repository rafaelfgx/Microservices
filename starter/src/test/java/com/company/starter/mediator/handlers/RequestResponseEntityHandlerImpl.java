package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Request;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.mediator.Response;
import org.springframework.http.ResponseEntity;

public class RequestResponseEntityHandlerImpl implements RequestResponseHandler<Request, ResponseEntity<Response>> {
    @Override
    public ResponseEntity<Response> handle(Request request) {
        return ResponseEntity.ok(new Response("RequestResponseEntityHandler"));
    }
}
