package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerMapper;
import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.CreateCustomerRequest;
import com.company.starter.clients.auth.AuthAmbassador;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CreateCustomerHandler implements RequestResponseHandler<CreateCustomerRequest, ResponseEntity<UUID>> {
    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final CreateCustomerValidatorHandler createCustomerValidatorHandler;
    private final AuthAmbassador authAmbassador;

    @Override
    public ResponseEntity<UUID> handle(final CreateCustomerRequest request) {
        createCustomerValidatorHandler.handle(request);
        final var user = customerMapper.toUserRequest(request);
        final var userId = authAmbassador.save(user).getBody();
        final var customer = customerMapper.toCustomer(request, userId);
        final var id = customerRepository.save(customer).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
}
