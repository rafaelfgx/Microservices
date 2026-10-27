package com.company.starter.otp;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
public class OtpService {
    private static final int LENGTH = 6;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String NUMERIC = "0123456789";
    private final SecureRandom random = new SecureRandom();

    public String generateNumeric() {
        return generate(NUMERIC);
    }

    public String generateAlphaNumeric() {
        return generate(ALPHANUMERIC);
    }

    private String generate(final String charset) {
        return random.ints(LENGTH, 0, charset.length()).mapToObj(charset::charAt).map(String::valueOf).collect(Collectors.joining());
    }
}
