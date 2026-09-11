package com.company.configurationservice.configuration;

import com.company.configurationservice.shared.Data;
import com.company.configurationservice.shared.MongoTestConfiguration;
import com.company.starter.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@Import(MongoTestConfiguration.class)
@DataMongoTest
class ConfigurationRepositoryTest {
    @Autowired
    MongoTemplate mongo;

    @Autowired
    ConfigurationRepository repository;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @Test
    void shouldReturnEmptyListWhenNoConfigurationsExist() {
        final var configurations = repository.findAllBy(Configuration.class);
        Assertions.assertThat(configurations).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyListWhenConfigurationsExist() {
        repository.save(Data.CONFIGURATION);
        final var configurations = repository.findAllBy(Configuration.class);
        Assertions.assertThat(configurations).hasSize(1);
        Assertions.assertThat(configurations.getFirst()).isEqualTo(Data.CONFIGURATION_RESPONSE);
    }

    @Test
    void shouldReturnEmptyWhenConfigurationDoesNotExist() {
        final var configuration = repository.findById(Data.ID, Configuration.class);
        Assertions.assertThat(configuration).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyWhenConfigurationExists() {
        repository.save(Data.CONFIGURATION);
        final var configuration = repository.findById(Data.ID, Configuration.class);
        Assertions.assertThat(configuration).isPresent();
        Assertions.assertThat(configuration.orElseThrow()).isEqualTo(Data.CONFIGURATION_RESPONSE);
    }
}
