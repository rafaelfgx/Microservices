package com.company.orderservice.shared;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@TestConfiguration
@Import({
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
}
