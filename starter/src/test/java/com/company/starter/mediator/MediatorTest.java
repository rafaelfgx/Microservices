package com.company.starter.mediator;

import com.company.starter.mediator.handlers.HandlerImpl;
import com.company.starter.mediator.handlers.RequestHandlerImpl;
import com.company.starter.mediator.handlers.RequestResponseEntityHandlerImpl;
import com.company.starter.mediator.handlers.RequestResponseEntityListHandlerImpl;
import com.company.starter.mediator.handlers.RequestResponseEntityPageHandlerImpl;
import com.company.starter.mediator.handlers.RequestResponseHandlerImpl;
import com.company.starter.mediator.handlers.ResponseHandlerImpl;
import com.company.starter.mediator.handlers.ResponseListHandlerImpl;
import com.company.starter.mediator.handlers.ResponsePageHandlerImpl;
import com.company.starter.validation.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;

@SpringBootTest(classes = {
    ValidationAutoConfiguration.class,
    Mediator.class,
    ValidationService.class,
    HandlerImpl.class,
    RequestHandlerImpl.class,
    ResponseHandlerImpl.class,
    ResponseListHandlerImpl.class,
    ResponsePageHandlerImpl.class,
    RequestResponseHandlerImpl.class,
    RequestResponseEntityHandlerImpl.class,
    RequestResponseEntityListHandlerImpl.class,
    RequestResponseEntityPageHandlerImpl.class
})
class MediatorTest {
    @Autowired
    Mediator mediator;

    @Test
    void shouldExecuteHandlerWhenHandlerIsDispatched() {
        mediator.handle(HandlerImpl.class);
    }

    @Test
    void shouldExecuteHandlerWhenRequestIsDispatched() {
        mediator.handleRequest(RequestHandlerImpl.class, new Request());
    }

    @Test
    void shouldReturnResponseWhenResponseHandlerIsDispatched() {
        final var response = mediator.handleResponse(ResponseHandlerImpl.class);
        Assertions.assertEquals("ResponseHandler", response.message());
    }

    @Test
    void shouldReturnListWhenResponseListHandlerIsDispatched() {
        final var response = mediator.handleResponse(ResponseListHandlerImpl.class);
        Assertions.assertEquals("ResponseListHandler", response.getFirst().message());
    }

    @Test
    void shouldReturnPageWhenResponsePageHandlerIsDispatched() {
        final var response = mediator.handleResponse(ResponsePageHandlerImpl.class);
        Assertions.assertEquals("ResponsePageHandler", response.get().findFirst().orElseThrow().message());
    }

    @Test
    void shouldReturnResponseWhenRequestResponseHandlerIsDispatched() {
        final var response = mediator.handle(RequestResponseHandlerImpl.class, new Request());
        Assertions.assertEquals("RequestResponseHandler", response.message());
    }

    @Test
    void shouldReturnResponseEntityWhenRequestResponseEntityHandlerIsDispatched() {
        final var response = mediator.handle(RequestResponseEntityHandlerImpl.class, new Request());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("RequestResponseEntityHandler", response.getBody().message());
    }

    @Test
    void shouldReturnResponseEntityListWhenRequestResponseEntityListHandlerIsDispatched() {
        final var response = mediator.handle(RequestResponseEntityListHandlerImpl.class, new Request());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("RequestResponseEntityListHandler", response.getBody().getFirst().message());
    }

    @Test
    void shouldReturnResponseEntityPageWhenRequestResponseEntityPageHandlerIsDispatched() {
        final var response = mediator.handle(RequestResponseEntityPageHandlerImpl.class, new Request());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("RequestResponseEntityPageHandler", response.getBody().get().findFirst().orElseThrow().message());
    }
}
