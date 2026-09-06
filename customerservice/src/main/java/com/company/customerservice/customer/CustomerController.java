package com.company.customerservice.customer;

import com.company.customerservice.customer.handlers.CreateCustomerHandler;
import com.company.customerservice.customer.handlers.DeleteCustomerHandler;
import com.company.customerservice.customer.handlers.GetCustomerHandler;
import com.company.customerservice.customer.handlers.ListCustomerHandler;
import com.company.customerservice.customer.handlers.UpdateCustomerHandler;
import com.company.customerservice.customer.requests.CreateCustomerRequest;
import com.company.customerservice.customer.requests.DeleteCustomerRequest;
import com.company.customerservice.customer.requests.GetCustomerRequest;
import com.company.customerservice.customer.requests.ListCustomerRequest;
import com.company.customerservice.customer.requests.UpdateCustomerRequest;
import com.company.customerservice.customer.responses.CustomerResponse;
import com.company.starter.mediator.Mediator;
import com.company.starter.swagger.DefaultApiResponse;
import com.company.starter.swagger.GetApiResponse;
import com.company.starter.swagger.PostApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Customers")
@RequestMapping("/customers")
@RequiredArgsConstructor
@RestController
public class CustomerController {
    private final Mediator mediator;

    @Operation(summary = "List")
    @GetApiResponse
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> list(@ParameterObject @Valid final ListCustomerRequest request, @ParameterObject @Valid final Pageable pageable) {
        return mediator.handle(ListCustomerHandler.class, request.withPageable(pageable));
    }

    @Operation(summary = "Get")
    @GetApiResponse
    @GetMapping("{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable final UUID id) {
        return mediator.handle(GetCustomerHandler.class, new GetCustomerRequest(id));
    }

    @Operation(summary = "Create")
    @PostApiResponse
    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody @Valid final CreateCustomerRequest request) {
        return mediator.handle(CreateCustomerHandler.class, request);
    }

    @Operation(summary = "Update")
    @DefaultApiResponse
    @PutMapping("{id}")
    public ResponseEntity<Void> update(@PathVariable final UUID id, @RequestBody @Valid final UpdateCustomerRequest request) {
        return mediator.handle(UpdateCustomerHandler.class, request.withId(id));
    }

    @Operation(summary = "Delete")
    @DefaultApiResponse
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable final UUID id) {
        return mediator.handle(DeleteCustomerHandler.class, new DeleteCustomerRequest(id));
    }
}
