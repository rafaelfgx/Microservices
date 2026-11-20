package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.UUID;

@DataMongoTest
@Import(MongoTestConfiguration.class)
class OutboxRepositoryTest {
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    OutboxRepository outboxRepository;

    @BeforeEach
    void beforeEach() {
        mongoTemplate.dropCollection(Outbox.class);
        mongoTemplate.save(new Outbox("topic1", "key1", "{}"));
        mongoTemplate.save(new Outbox("topic1", "key2", "{}"));
        mongoTemplate.save(new Outbox("topic2", "key1", "{}"));
    }

    @Test
    void shouldFindByTopic() {
        final var page = outboxRepository.findByTopic("topic1", PageRequest.of(0, 1));
        Assertions.assertEquals(2, page.getTotalElements());
        Assertions.assertEquals(1, page.getContent().size());
        Assertions.assertTrue(outboxRepository.findByTopic("missing", Pageable.unpaged()).isEmpty());
    }

    @Test
    void shouldFindByKey() {
        final var page = outboxRepository.findByKey("key1", PageRequest.of(0, 1));
        Assertions.assertEquals(2, page.getTotalElements());
        Assertions.assertEquals(1, page.getContent().size());
        Assertions.assertTrue(outboxRepository.findByKey("missing", Pageable.unpaged()).isEmpty());
    }

    @Test
    void shouldFindByAttemptsGreaterThanEqual() {
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 3, null, null));
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 5, null, null));
        Assertions.assertEquals(2, outboxRepository.findByAttemptsGreaterThanEqual(3, Pageable.unpaged()).getTotalElements());
        Assertions.assertEquals(1, outboxRepository.findByAttemptsGreaterThanEqual(4, Pageable.unpaged()).getTotalElements());
        Assertions.assertEquals(1, outboxRepository.findByAttemptsGreaterThanEqual(3, PageRequest.of(0, 1)).getContent().size());
        Assertions.assertTrue(outboxRepository.findByAttemptsGreaterThanEqual(6, Pageable.unpaged()).isEmpty());
    }
}
