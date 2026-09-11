package com.company.starter.clients.auth;

import com.company.starter.clients.ClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class AuthConfiguration {
    private final ClientFactory factory;

    @Conditional(AuthCondition.class)
    @Bean
    public AuthClient authClient(final AuthProperties properties) {
        return factory.create(AuthClient.class, properties);
    }
}
