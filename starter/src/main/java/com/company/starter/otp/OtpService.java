package com.company.starter.otp;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpService {
    private static final int LENGTH = 6;
    private static final char[] ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final char[] NUMERIC = "0123456789".toCharArray();
    private final SecureRandom random = new SecureRandom();

    public String generateNumeric() {
        return generate(NUMERIC);
    }

    public String generateAlphaNumeric() {
        return generate(ALPHANUMERIC);
    }

    private String generate(final char[] charset) {
        final var builder = new StringBuilder(LENGTH);

        for (int index = 0; index < LENGTH; index++) {
            builder.append(charset[random.nextInt(charset.length)]);
        }

        return builder.toString();
    }
}
