package com.company.starter.mediator.handlers;

import com.company.starter.mediator.Response;
import com.company.starter.mediator.ResponseHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public class ResponsePageHandlerImpl implements ResponseHandler<Page<Response>> {
    @Override
    public Page<Response> handle() {
        return new PageImpl<>(List.of(new Response("ResponsePageHandler")));
    }
}
