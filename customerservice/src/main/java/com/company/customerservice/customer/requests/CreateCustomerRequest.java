package com.company.customerservice.customer.requests;

import com.company.starter.validation.password.Password;
import com.company.starter.validation.username.Username;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;

public record CreateCustomerRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank @Username String username,
    @NotBlank @Password String password) {

    public CreateCustomerRequest {
        email = StringUtils.lowerCase(email);
        username = StringUtils.lowerCase(username);
    }
}
