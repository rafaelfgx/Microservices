package com.company.starter.otp;

import com.company.starter.exception.ApplicationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

class OtpServiceTest {
    @Test
    void shouldRejectCodeWhenAnotherRequestAlreadyConsumedIt() {
        final PasswordEncoder encoder = Mockito.mock();
        final OtpRepository repository = Mockito.mock();
        final Otp otp = Mockito.mock();
        final var id = UUID.randomUUID();
        final var service = new OtpService(encoder, repository, new OtpProperties(Duration.ofMinutes(5), 6, 3));
        Mockito.when(repository.consume(Mockito.eq("login"), Mockito.eq("user"), Mockito.any(), Mockito.eq(3))).thenReturn(Optional.of(otp));
        Mockito.when(otp.getId()).thenReturn(id);
        Mockito.when(otp.getHash()).thenReturn("hash");
        Mockito.when(encoder.matches("123456", "hash")).thenReturn(true);
        Mockito.when(repository.delete(id)).thenReturn(false);
        Assertions
            .assertThatThrownBy(() -> service.validate(new ValidateOtpRequest("login", "user", "123456")))
            .isInstanceOfSatisfying(ApplicationException.class, exception -> {
                Assertions.assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                Assertions.assertThat(exception.getBody().getDetail()).isEqualTo("OTP is invalid or expired");
            });
        Mockito.verify(repository).delete(id);
    }
}
