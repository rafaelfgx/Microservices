package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
import com.company.starter.configurations.SchedulerConfiguration;
import org.bson.Document;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@DataMongoTest
@TestPropertySource(properties = {
    "outbox.delay=PT1S",
    "outbox.lock=PT2S",
    "outbox.limit=10",
    "outbox.attempts=3"
})
@Import({
    KafkaAutoConfiguration.class,
    MongoTestConfiguration.class,
    SchedulerConfiguration.class,
    OutboxScheduler.class
})
@EnableConfigurationProperties(OutboxProperties.class)
class OutboxSchedulerTest {
    @MockitoBean
    KafkaTemplate<String, String> kafkaTemplate;

    @MockitoSpyBean
    OutboxRepository outboxRepository;

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
        Assertions.assertEquals(0, mongoTemplate.count(new Query(), Outbox.class));
    }

    @Test
    void shouldDeleteSentOutboxesAfterSuccessfulPublish() {
        final var outbox = new Outbox("topic", "key", "{}");
        mongoTemplate.save(outbox);
        Mockito.when(kafkaTemplate.send("topic", "key", "{}")).thenReturn(CompletableFuture.completedFuture(null));
        outboxScheduler.handle();
        Mockito.verify(outboxRepository).deleteAllById(List.of(outbox.getId()));
    }

    @Test
    void shouldIgnoreExceptionalSendResultWhenClaimedOutboxFails() {
        final var outbox = new Outbox("topic", "key", "{}");
        mongoTemplate.save(outbox);

        final var failed = new CompletableFuture<SendResult<String, String>>() {
            @Override
            public <U> @NonNull CompletableFuture<U> handle(final @NonNull BiFunction<? super SendResult<String, String>, Throwable, ? extends U> fn) {
                return CompletableFuture.failedFuture(new RuntimeException("send error"));
            }
        };

        Mockito.when(kafkaTemplate.send("topic", "key", "{}")).thenReturn(failed);
        Assertions.assertDoesNotThrow(outboxScheduler::handle);
        Mockito.verify(outboxRepository, Mockito.never()).deleteAllById(Mockito.any());
    }

    @Test
    void shouldIncrementAttemptsWhenHandleFails() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));
        final var outbox = new Outbox("topic", "key", "{}");
        mongoTemplate.save(outbox);
        outboxScheduler.handle();
        outboxScheduler.handle();
        outboxScheduler.handle();
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getAttempts());
    }

    @Test
    void shouldHandleRepositoryClaimException() {
        mongoTemplate.getCollection("outbox").insertOne(new Document()
            .append("_id", "invalid-uuid")
            .append("timestamp", Instant.now())
            .append("topic", "topic")
            .append("key", "key")
            .append("data", "{}")
            .append("attempts", 0));
        Assertions.assertDoesNotThrow(outboxScheduler::handle);
        Assertions.assertEquals(1, mongoTemplate.getCollection("outbox").countDocuments());
    }

    @Test
    void shouldHandleSynchronousKafkaSendException() {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException("send error"));
        final var outbox = new Outbox("topic", "key", "{}");
        mongoTemplate.save(outbox);
        Assertions.assertDoesNotThrow(outboxScheduler::handle);
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAttempts());
    }
}
