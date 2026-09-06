package com.company.starter.configurations;

import com.company.starter.validation.ValidationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.company")
@Configuration
public class MongoConfiguration {
    @ConditionalOnBean(MongoDatabaseFactory.class)
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
