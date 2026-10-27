package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.GetCustomerRequest;
import com.company.customerservice.customer.responses.CustomerResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetCustomerHandler implements RequestResponseHandler<GetCustomerRequest, ResponseEntity<CustomerResponse>> {
    private final CustomerRepository customerRepository;

    @Override
    public ResponseEntity<CustomerResponse> handle(final GetCustomerRequest request) {
        return ResponseEntity.of(customerRepository.findById(request.id(), CustomerResponse.class));
    }
}
