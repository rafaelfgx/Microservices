package com.company.customerservice.customer.handlers;

import com.company.customerservice.customer.CustomerRepository;
import com.company.customerservice.customer.requests.VerifyCustomerRequest;
import com.company.customerservice.shared.Data;
import com.company.starter.clients.auth.AuthAmbassador;
import com.company.starter.otp.OtpService;
import com.company.starter.otp.ValidateOtpRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Optional;
import java.util.UUID;

class VerifyCustomerHandlerTest {
    private final CustomerRepository customerRepository = Mockito.mock();
    private final AuthAmbassador authAmbassador = Mockito.mock();
    private final OtpService otpService = Mockito.mock();
    private final VerifyCustomerHandler handler = new VerifyCustomerHandler(customerRepository, authAmbassador, otpService);
    private final VerifyCustomerRequest request = new VerifyCustomerRequest(UUID.fromString(Data.ID), "123456");

    @Test
    void shouldRejectUnknownCustomer() {
        Mockito.when(customerRepository.findById(request.id())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> handler.handle(request))
            .isInstanceOfSatisfying(ErrorResponseException.class, exception -> {
                Assertions.assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                Assertions.assertThat(exception.getBody().getDetail()).isEqualTo("Customer not found");
            });
        final var order = Mockito.inOrder(otpService, customerRepository);
        order.verify(otpService).validate(new ValidateOtpRequest("CUSTOMER_VERIFICATION", Data.ID, request.otp()));
        order.verify(customerRepository).findById(request.id());
        Mockito.verifyNoInteractions(authAmbassador);
    }

    @Test
    void shouldRejectInvalidOtp() {
        final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "OTP is invalid or expired");
        Mockito.doThrow(new ErrorResponseException(HttpStatus.BAD_REQUEST, problem, null))
            .when(otpService).validate(new ValidateOtpRequest("CUSTOMER_VERIFICATION", Data.ID, request.otp()));

        Assertions.assertThatThrownBy(() -> handler.handle(request))
            .isInstanceOfSatisfying(ErrorResponseException.class, exception -> {
                Assertions.assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                Assertions.assertThat(exception.getBody().getDetail()).isEqualTo("OTP is invalid or expired");
            });
        Mockito.verifyNoInteractions(customerRepository, authAmbassador);
    }

    @Test
    void shouldVerifyCustomerWhenOtpIsValid() {
        Mockito.when(customerRepository.findById(request.id())).thenReturn(Optional.of(Data.CUSTOMER));
        Assertions.assertThat(handler.handle(request).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Mockito.verify(otpService).validate(new ValidateOtpRequest("CUSTOMER_VERIFICATION", Data.ID, request.otp()));
        Mockito.verify(authAmbassador).verify(Data.USER_ID);
    }
}
