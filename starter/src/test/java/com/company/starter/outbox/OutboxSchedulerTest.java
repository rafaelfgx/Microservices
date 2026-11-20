package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@DataMongoTest
@TestPropertySource(properties = {
    "outbox.delay=PT1S",
    "outbox.lock=PT1S",
    "outbox.limit=5"
})
@EnableConfigurationProperties(OutboxProperties.class)
@Import({
    MongoTestConfiguration.class,
    OutboxSchedulerRepository.class,
    OutboxScheduler.class
})
class OutboxSchedulerTest {
    @MockitoBean
    KafkaTemplate<String, String> kafkaTemplate;

    @MockitoSpyBean
    OutboxSchedulerRepository outboxSchedulerRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    OutboxScheduler outboxScheduler;

    @BeforeEach
    void beforeEach() {
        mongoTemplate.dropCollection(Outbox.class);
    }

    @Test
    void shouldRemoveOutboxWhenHandleSucceeds() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));
        mongoTemplate.save(new Outbox("topic", "key", "{}"));
        outboxScheduler.schedule();
        Mockito.verify(kafkaTemplate).send("topic", "key", "{}");
        Assertions.assertEquals(0, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldRemoveSentAndKeepFailedOutboxLockedWhenBatchPartiallySucceeds() {
        Mockito.when(kafkaTemplate.send(Mockito.eq("success"), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));
        Mockito.when(kafkaTemplate.send(Mockito.eq("failure"), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("send error")));
        final var success = mongoTemplate.save(new Outbox("success", "key", "{}"));
        final var failure = mongoTemplate.save(new Outbox("failure", "key", "{}"));
        outboxScheduler.schedule();
        Assertions.assertNull(mongoTemplate.findById(success.getId(), Outbox.class));
        final var result = mongoTemplate.findById(failure.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
        Assertions.assertNotNull(result.getLockId());
        Assertions.assertNotNull(result.getLockExpiration());
        Assertions.assertEquals(1, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldKeepOutboxClaimedWhenSendTimesOut() {
        Mockito.when(kafkaTemplate.send(Mockito.eq("pending"), Mockito.anyString(), Mockito.anyString())).thenReturn(new CompletableFuture<>());
        Mockito.when(kafkaTemplate.send(Mockito.eq("success"), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));
        final var pending = mongoTemplate.save(new Outbox("pending", "key", "{}"));
        final var success = mongoTemplate.save(new Outbox("success", "key", "{}"));
        outboxScheduler.schedule();
        Assertions.assertNull(mongoTemplate.findById(success.getId(), Outbox.class));
        final var result = mongoTemplate.findById(pending.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
        Assertions.assertNotNull(result.getLockId());
        Assertions.assertNotNull(result.getLockExpiration());
    }

    @Test
    void shouldKeepOutboxClaimedWhenHandleFails() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        outboxScheduler.schedule();
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
        Assertions.assertNotNull(result.getLockId());
        Assertions.assertNotNull(result.getLockExpiration());
    }

    @Test
    void shouldClaimOutboxAgainAfterClaimLockExpires() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        outboxScheduler.schedule();
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(outbox.getId())), new Update().set("lockExpiration", Instant.EPOCH), Outbox.class);
        outboxScheduler.schedule();
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getAttempts());
        Assertions.assertNotNull(result.getLockId());
        Assertions.assertNotNull(result.getLockExpiration());
        Mockito.verify(kafkaTemplate, Mockito.times(2)).send("topic", "key", "{}");
    }

    @Test
    void shouldSkipOutboxWhenWaitingForRetry() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 1, null, Instant.now().plusSeconds(60)));
        outboxScheduler.schedule();
        Mockito.verifyNoInteractions(kafkaTemplate);
        Assertions.assertEquals(1, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldHandleRepositoryClaimException() {
        Mockito.doThrow(new RuntimeException("claim error")).when(outboxSchedulerRepository).claim(Mockito.anyString());
        mongoTemplate.save(new Outbox("topic", "key", "{}"));
        Assertions.assertDoesNotThrow(outboxScheduler::schedule);
        Mockito.verify(kafkaTemplate, Mockito.never()).send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Assertions.assertEquals(1, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldHandleSynchronousKafkaSendException() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException("send error"));
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        Assertions.assertDoesNotThrow(outboxScheduler::schedule);
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
        Assertions.assertNotNull(result.getLockId());
        Assertions.assertNotNull(result.getLockExpiration());
    }
}
