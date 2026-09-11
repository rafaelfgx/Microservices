package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerMapper;
import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.UpdateCustomerRequest;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UpdateCustomerHandler implements RequestResponseHandler<UpdateCustomerRequest, ResponseEntity<Void>> {
    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    @Override
    public ResponseEntity<Void> handle(final UpdateCustomerRequest request) {
        customerRepository
            .findById(request.id())
            .ifPresent(customer -> customerRepository.save(customerMapper.toCustomer(customer, request)));

        return ResponseEntity.noContent().build();
    }
}
