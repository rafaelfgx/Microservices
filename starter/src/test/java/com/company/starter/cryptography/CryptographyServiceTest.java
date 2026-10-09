package com.company.starter.cryptography;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CryptographyService.class, properties = "cryptography.password=SuperSecretPassword")
class CryptographyServiceTest {
    @Autowired
    CryptographyService cryptographyService;

    @Test
    void shouldReturnOriginalValueWhenDecryptingEncryptedValue() {
        final var salt = cryptographyService.salt();
        final var encrypted = cryptographyService.encrypt("value", salt);
        Assertions.assertNotEquals("value", encrypted);
        Assertions.assertEquals("value", cryptographyService.decrypt(encrypted, salt));
    }
}
