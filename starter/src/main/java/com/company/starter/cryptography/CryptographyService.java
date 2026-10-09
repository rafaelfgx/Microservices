package com.company.starter.cryptography;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.encrypt.AesGcmBytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ConditionalOnProperty(name = "cryptography.password")
@Service
public class CryptographyService {
    private final String password;

    public CryptographyService(@Value("${cryptography.password}") final String password) {
        this.password = password;
    }

    public String salt() {
        return KeyGenerators.string().generateKey();
    }

    public String encrypt(final String value, final String salt) {
        return Base64.getEncoder().encodeToString(encryptor(salt).encrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    public String decrypt(final String value, final String salt) {
        return new String(encryptor(salt).decrypt(Base64.getDecoder().decode(value)), StandardCharsets.UTF_8);
    }

    private AesGcmBytesEncryptor encryptor(final String salt) {
        return AesGcmBytesEncryptor.withPassword(password, salt).build();
    }
}
