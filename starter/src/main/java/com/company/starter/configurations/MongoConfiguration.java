package com.company.starter.configurations;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.mapping.event.ValidatingEntityCallback;

@Configuration(proxyBeanMethods = false)
public class MongoConfiguration {
    @Bean
    public MongoTransactionManager mongoTransactionManager(final MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    @Bean
    public ValidatingEntityCallback mongoValidatingEntityCallback(final Validator validator) {
        return new ValidatingEntityCallback(validator);
    }
}
