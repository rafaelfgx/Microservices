package com.company.customerservice.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authCustomizer() {
        return registry -> {
            registry.requestMatchers(HttpMethod.POST, "/customers").permitAll();
            registry.requestMatchers(HttpMethod.PATCH, "/customers/{id}/verify").permitAll();
        };
    }
}
