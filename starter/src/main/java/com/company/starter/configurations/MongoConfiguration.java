package com.company.starter.configurations;

import com.company.starter.validation.ValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.company")
@Configuration
public class MongoConfiguration {
    @Bean
    public MongoTransactionManager mongoTransactionManager(final MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    @Bean
    public BeforeConvertCallback<Object> mongoBeforeConvertCallback(final ValidationService validationService) {
        return (entity, collection) -> {
            validationService.validateOrThrow(entity);
            return entity;
        };
    }
}
