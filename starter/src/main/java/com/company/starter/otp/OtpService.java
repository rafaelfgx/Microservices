package com.company.starter.otp;

import com.company.starter.exception.ApplicationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.stream.Collectors;

@Validated
@RequiredArgsConstructor
@Service
public class OtpService {
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();
    private final OtpRepository repository;
    private final OtpProperties properties;

    public String generate(@Valid final GenerateOtpRequest request) {
        final var code = generate();
        repository.upsert(request.type(), request.key(), encoder.encode(code));
        return code;
    }

    public void validate(@Valid final ValidateOtpRequest request) {
        repository
            .consume(request.type(), request.key(), Instant.now().minus(properties.expiration()), properties.attempts())
            .filter(otp -> encoder.matches(request.code(), otp.getHash()))
            .filter(otp -> repository.delete(otp.getId()))
            .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "OTP is invalid or expired"));
    }

    private String generate() {
        return random.ints(properties.length(), 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
    }
}
