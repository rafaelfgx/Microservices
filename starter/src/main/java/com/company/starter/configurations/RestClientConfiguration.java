package com.company.starter.configurations;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration(proxyBeanMethods = false)
public class RestClientConfiguration {
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> builder.defaultStatusHandler(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND), (_, _) -> {});
    }
}
