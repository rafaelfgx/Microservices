package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Request;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.mediator.Response;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class RequestResponseEntityListHandlerImpl implements RequestResponseHandler<Request, ResponseEntity<List<Response>>> {
    @Override
    public ResponseEntity<List<Response>> handle(Request request) {
        return ResponseEntity.ok(List.of(new Response("RequestResponseEntityListHandler")));
    }
}
