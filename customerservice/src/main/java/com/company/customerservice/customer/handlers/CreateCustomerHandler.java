package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerMapper;
import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.CreateCustomerRequest;
import com.company.starter.clients.auth.AuthAmbassador;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.otp.GenerateOtpRequest;
import com.company.starter.otp.OtpService;
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
    private final AuthAmbassador authAmbassador;
    private final OtpService otpService;

    @Override
    public ResponseEntity<UUID> handle(final CreateCustomerRequest request) {
        final var user = customerMapper.toSaveUserRequest(request);
        final var userId = authAmbassador.save(user).getBody();
        final var customer = customerMapper.toCustomer(request, userId);
        final var customerId = customerRepository.findCustomerIdByUserId(userId).orElseGet(customer::getId);
        customerRepository.save(customer.withId(customerId));
        final var otp = otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", customerId.toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(customerId);
    }
}
