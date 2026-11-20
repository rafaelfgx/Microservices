package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerError;
import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.CreateCustomerRequest;
import com.company.starter.exception.ApplicationException;
import com.company.starter.mediator.RequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CreateCustomerValidatorHandler implements RequestHandler<CreateCustomerRequest> {
    private final CustomerRepository customerRepository;

    @Override
    public void handle(final CreateCustomerRequest request) {
        if (customerRepository.existsByEmailOrUsername(request.email(), request.username())) {
            throw new ApplicationException(CustomerError.CUSTOMER_EXISTS);
        }
    }
}
