package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
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
import tools.jackson.databind.json.JsonMapper;

@DataMongoTest
@Import({
    MongoTestConfiguration.class,
    OutboxService.class,
    OutboxServiceTest.OutboxServiceTestConfiguration.class
})
class OutboxServiceTest {
    private static final String TOPIC = "topic";
    private static final String KEY = "key";

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
    void shouldIgnoreWhenDataIsNull() {
        transactionTemplate.executeWithoutResult(_ -> outboxService.save(TOPIC, KEY, null));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldIgnoreWhenTopicIsNullOrBlank(final String topic) {
        transactionTemplate.executeWithoutResult(_ -> outboxService.save(topic, KEY, "value"));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldIgnoreWhenKeyIsNullOrBlank(final String key) {
        transactionTemplate.executeWithoutResult(_ -> outboxService.save(TOPIC, key, "value"));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @Test
    void shouldSaveOutboxWhenDataIsObject() {
        transactionTemplate.executeWithoutResult(_ -> outboxService.save(TOPIC, KEY, "value"));
        final var outboxes = mongoTemplate.findAll(Outbox.class);
        Assertions.assertEquals(1, outboxes.size());
        Assertions.assertEquals(TOPIC, outboxes.getFirst().getTopic());
        Assertions.assertEquals(KEY, outboxes.getFirst().getKey());
        Assertions.assertEquals("\"value\"", outboxes.getFirst().getData());
    }

    @Test
    void shouldFailWhenCalledWithoutTransaction() {
        Assertions.assertThrows(IllegalTransactionStateException.class, () -> outboxService.save(TOPIC, KEY, "value"));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @Test
    void shouldNotPersistWhenTransactionRollsBack() {
        Assertions.assertThrows(RuntimeException.class, () -> transactionTemplate.executeWithoutResult(_ -> {
            outboxService.save(TOPIC, KEY, "value");
            throw new RuntimeException("rollback");
        }));
        Assertions.assertTrue(mongoTemplate.findAll(Outbox.class).isEmpty());
    }

    @TestConfiguration
    static class OutboxServiceTestConfiguration {
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
    }
}
