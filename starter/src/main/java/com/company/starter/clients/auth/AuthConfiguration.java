package com.company.starter.clients.auth;

import com.company.starter.clients.ClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AuthConfiguration {
    @Conditional(AuthCondition.class)
    @Bean
    public AuthClient authClient(final ClientFactory factory, final AuthProperties properties) {
        return factory.create(AuthClient.class, properties);
    }
}
