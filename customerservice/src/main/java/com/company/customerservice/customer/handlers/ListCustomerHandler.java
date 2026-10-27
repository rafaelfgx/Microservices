package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.ListCustomerRequest;
import com.company.customerservice.customer.responses.CustomerResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ListCustomerHandler implements RequestResponseHandler<ListCustomerRequest, ResponseEntity<Page<CustomerResponse>>> {
    private final CustomerRepository customerRepository;

    @Override
    public ResponseEntity<Page<CustomerResponse>> handle(final ListCustomerRequest request) {
        return ResponseEntity.of(Optional.of(customerRepository.findBy(request)).filter(Page::hasContent));
    }
}
