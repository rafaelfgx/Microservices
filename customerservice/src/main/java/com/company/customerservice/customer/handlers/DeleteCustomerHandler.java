package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.domains.Customer;
import com.company.customerservice.customer.requests.DeleteCustomerRequest;
import com.company.starter.clients.auth.AuthAmbassador;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DeleteCustomerHandler implements RequestResponseHandler<DeleteCustomerRequest, ResponseEntity<Void>> {
    private final CustomerRepository customerRepository;
    private final AuthAmbassador authAmbassador;

    @Override
    public ResponseEntity<Void> handle(final DeleteCustomerRequest request) {
        customerRepository.findById(request.id()).ifPresent(this::delete);
        return ResponseEntity.noContent().build();
    }

    private void delete(final Customer customer) {
        authAmbassador.delete(customer.getUserId());
        customerRepository.deleteById(customer.getId());
    }
}
