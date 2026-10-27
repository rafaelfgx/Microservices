package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Request;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.mediator.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class RequestResponseEntityPageHandlerImpl implements RequestResponseHandler<Request, ResponseEntity<Page<Response>>> {
    @Override
    public ResponseEntity<Page<Response>> handle(Request request) {
        return ResponseEntity.ok(new PageImpl<>(List.of(new Response("RequestResponseEntityPageHandler"))));
    }
}
