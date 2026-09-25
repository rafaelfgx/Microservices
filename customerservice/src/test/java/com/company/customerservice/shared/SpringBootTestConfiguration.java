package com.company.customerservice.shared;

import com.company.starter.clients.auth.AuthClient;
import com.company.starter.exception.GlobalExceptionHandler;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.UUID;

@TestConfiguration
@Import({
    GlobalExceptionHandler.class,
    KafkaTestConfiguration.class,
    MongoTestConfiguration.class,
    RedisTestConfiguration.class
})
public class SpringBootTestConfiguration {
    @Primary
    @Bean
    public ClientRegistrationRepository clientRegistrationRepositoryMock() {
        return Mockito.mock(ClientRegistrationRepository.class);
    }

    @Primary
    @Bean
    public AuthClient authClientMock() {
        final var authClient = Mockito.mock(AuthClient.class);
        Mockito.when(authClient.save(ArgumentMatchers.any())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(UUID.randomUUID()));
        Mockito.when(authClient.delete(ArgumentMatchers.any())).thenReturn(ResponseEntity.noContent().build());
        return authClient;
    }
}
