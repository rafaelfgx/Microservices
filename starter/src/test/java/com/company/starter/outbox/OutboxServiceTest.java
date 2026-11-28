package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import tools.jackson.databind.json.JsonMapper;

@DataMongoTest
@Import({
    MongoTestConfiguration.class,
    OutboxService.class,
    OutboxServiceTest.Configuration.class
})
class OutboxServiceTest {
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    OutboxService outboxService;

    @BeforeEach
    void beforeEach() {
        mongoTemplate.dropCollection(Outbox.class);
        mongoTemplate.createCollection(Outbox.class);
    }

    @Test
    void shouldFailWhenDataIsNull() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionTemplate.executeWithoutResult(_ -> outboxService.save("topic", "key", null)));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldFailWhenTopicIsNullOrBlank(final String topic) {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionTemplate.executeWithoutResult(_ -> outboxService.save(topic, "key", "value")));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldFailWhenKeyIsNullOrBlank(final String key) {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionTemplate.executeWithoutResult(_ -> outboxService.save("topic", key, "value")));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @Test
    void shouldSaveOutboxWhenDataIsObject() {
        transactionTemplate.executeWithoutResult(_ -> outboxService.save("topic", "key", "value"));
        final var outboxes = mongoTemplate.findAll(Outbox.class);
        Assertions.assertEquals(1, outboxes.size());
        Assertions.assertEquals("topic", outboxes.getFirst().getTopic());
        Assertions.assertEquals("key", outboxes.getFirst().getKey());
        Assertions.assertEquals("\"value\"", outboxes.getFirst().getData());
    }

    @Test
    void shouldFailWhenCalledWithoutTransaction() {
        Assertions.assertThrows(IllegalTransactionStateException.class, () -> outboxService.save("topic", "key", "value"));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @Test
    void shouldNotPersistWhenTransactionRollsBack() {
        Assertions.assertThrows(RuntimeException.class, () -> transactionTemplate.executeWithoutResult(_ -> {
            outboxService.save("topic", "key", "value");
            throw new RuntimeException("rollback");
        }));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }

        @Bean
        MongoTransactionManager mongoTransactionManager(final MongoDatabaseFactory factory) {
            return new MongoTransactionManager(factory);
        }

        @Bean
        TransactionTemplate transactionTemplate(final MongoTransactionManager mongoTransactionManager) {
            return new TransactionTemplate(mongoTransactionManager);
        }

        @Bean
        MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
    }
}
