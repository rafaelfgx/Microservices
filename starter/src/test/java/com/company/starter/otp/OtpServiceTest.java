package com.company.starter.otp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OtpServiceTest {
    OtpService otpService = new OtpService();

    @Test
    void shouldReturnSixDigitCodeWhenGeneratingNumeric() {
        Assertions.assertTrue(otpService.generateNumeric().matches("\\d{6}"));
    }

    @Test
    void shouldReturnSixCharacterCodeWhenGeneratingAlphaNumeric() {
        Assertions.assertTrue(otpService.generateAlphaNumeric().matches("[A-Z0-9]{6}"));
    }
}
