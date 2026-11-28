package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.VerifyCustomerRequest;
import com.company.customerservice.shared.Error;
import com.company.starter.clients.auth.AuthAmbassador;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.otp.OtpService;
import com.company.starter.otp.ValidateOtpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VerifyCustomerHandler implements RequestResponseHandler<VerifyCustomerRequest, ResponseEntity<Void>> {
    private final CustomerRepository customerRepository;
    private final AuthAmbassador authAmbassador;
    private final OtpService otpService;

    @Override
    public ResponseEntity<Void> handle(final VerifyCustomerRequest request) {
        otpService.validate(new ValidateOtpRequest("CUSTOMER_VERIFICATION", request.id().toString(), request.otp()));
        final var customer = customerRepository.findById(request.id()).orElseThrow(Error.CUSTOMER_NOT_FOUND::exception);
        authAmbassador.verify(customer.getUserId());
        return ResponseEntity.noContent().build();
    }
}
