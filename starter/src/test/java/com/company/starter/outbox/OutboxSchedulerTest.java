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
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@DataMongoTest
@TestPropertySource(properties = {
    "outbox.delay=PT1S",
    "outbox.lock=PT5S",
    "outbox.limit=5",
    "outbox.retry-initial-delay=PT1S",
    "outbox.retry-max-delay=PT10S",
    "outbox.retry-multiplier=2.0"
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
        outboxScheduler.handle();
        Mockito.verify(kafkaTemplate).send("topic", "key", "{}");
        Assertions.assertEquals(0, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldRemoveSentAndScheduleRetryForFailedWhenBatchPartiallySucceeds() {
        Mockito.when(kafkaTemplate.send(Mockito.eq("success"), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));
        Mockito.when(kafkaTemplate.send(Mockito.eq("failure"), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("send error")));
        final var success = mongoTemplate.save(new Outbox("success", "key", "{}"));
        final var failure = mongoTemplate.save(new Outbox("failure", "key", "{}"));
        final var before = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        outboxScheduler.handle();
        Assertions.assertNull(mongoTemplate.findById(success.getId(), Outbox.class));
        final var result = mongoTemplate.findById(failure.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
        Assertions.assertNull(result.getLockId());
        Assertions.assertFalse(result.getLockUntil().isBefore(before.plusSeconds(1)));
        Assertions.assertEquals(1, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldKeepOutboxWhenCompletionFutureFails() {
        final CompletableFuture<SendResult<String, String>> future = Mockito.mock();
        Mockito.when(future.handle(Mockito.any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("send error")));
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(future);
        mongoTemplate.save(new Outbox("topic", "key", "{}"));
        outboxScheduler.handle();
        Assertions.assertEquals(1, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldScheduleRetryAndReleaseLockWhenHandleFails() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        final var before = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        outboxScheduler.handle();
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
        Assertions.assertNull(result.getLockId());
        Assertions.assertFalse(result.getLockUntil().isBefore(before.plusSeconds(1)));
    }

    @Test
    void shouldKeepRetryingWithBackoffWhenHandleFailsRepeatedly() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        outboxScheduler.handle();
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(outbox.getId())), new Update().unset("lockUntil"), Outbox.class);
        outboxScheduler.handle();
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(outbox.getId())), new Update().unset("lockUntil"), Outbox.class);
        outboxScheduler.handle();
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getAttempts());
        Assertions.assertNull(result.getLockId());
        Assertions.assertTrue(result.getLockUntil().isAfter(Instant.now().plusSeconds(3)));
        Mockito.verify(kafkaTemplate, Mockito.times(3)).send("topic", "key", "{}");
    }

    @Test
    void shouldSkipOutboxWhenWaitingForRetry() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.completedFuture(null));
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 1, null, Instant.now().plusSeconds(60)));
        outboxScheduler.handle();
        Mockito.verifyNoInteractions(kafkaTemplate);
        Assertions.assertEquals(1, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldHandleRepositoryClaimException() {
        Mockito.doThrow(new RuntimeException("claim error")).when(outboxSchedulerRepository).claim(Mockito.anyString());
        mongoTemplate.save(new Outbox("topic", "key", "{}"));
        Assertions.assertDoesNotThrow(outboxScheduler::handle);
        Mockito.verify(kafkaTemplate, Mockito.never()).send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Assertions.assertEquals(1, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldHandleSynchronousKafkaSendException() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException("send error"));
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        Assertions.assertDoesNotThrow(outboxScheduler::handle);
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
        Assertions.assertNull(result.getLockId());
        Assertions.assertTrue(result.getLockUntil().isAfter(Instant.now()));
    }
}
