package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@DataMongoTest
@TestPropertySource(properties = {
    "outbox.delay=PT1S",
    "outbox.lock=PT2S",
    "outbox.limit=2",
    "outbox.retry-initial-delay=PT1S",
    "outbox.retry-max-delay=PT10S",
    "outbox.retry-multiplier=2.0"
})
@EnableConfigurationProperties(OutboxProperties.class)
@Import({
    MongoTestConfiguration.class,
    OutboxSchedulerRepository.class
})
class OutboxSchedulerRepositoryTest {
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    OutboxSchedulerRepository outboxSchedulerRepository;

    @BeforeEach
    void beforeEach() {
        mongoTemplate.dropCollection(Outbox.class);
    }

    @Test
    void shouldReturnEmptyWhenMissing() {
        Assertions.assertTrue(outboxSchedulerRepository.claim("id").isEmpty());
    }

    @Test
    void shouldReturnNonEmptyWhenStatusPending() {
        mongoTemplate.save(new Outbox("topic", "key", "{}"));
        final var result = outboxSchedulerRepository.claim("id");
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("id", result.getFirst().getLockId());
        Assertions.assertNotNull(result.getFirst().getLockUntil());
    }

    @Test
    void shouldReturnNonEmptyWhenLockExpired() {
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 0, "old", Instant.now().minus(Duration.ofHours(2))));
        final var result = outboxSchedulerRepository.claim("new");
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("new", result.getFirst().getLockId());
    }

    @Test
    void shouldReturnEmptyWhenLocked() {
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 0, "old", Instant.now().plusSeconds(60)));
        Assertions.assertTrue(outboxSchedulerRepository.claim("new").isEmpty());
    }

    @Test
    void shouldReleaseLockAndScheduleRetryWithBackoff() {
        final var first = mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 1, "lock", Instant.now().plusSeconds(60)));
        final var second = mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 3, "lock", Instant.now().plusSeconds(60)));
        final var before = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        outboxSchedulerRepository.retry("lock", List.of(first, second));
        final var firstResult = mongoTemplate.findById(first.getId(), Outbox.class);
        final var secondResult = mongoTemplate.findById(second.getId(), Outbox.class);
        Assertions.assertNotNull(firstResult);
        Assertions.assertNotNull(secondResult);
        Assertions.assertNull(firstResult.getLockId());
        Assertions.assertNull(secondResult.getLockId());
        Assertions.assertFalse(firstResult.getLockUntil().isBefore(before.plusSeconds(1)));
        Assertions.assertFalse(secondResult.getLockUntil().isBefore(before.plusSeconds(4)));
        Assertions.assertTrue(secondResult.getLockUntil().isAfter(firstResult.getLockUntil()));
    }

    @Test
    void shouldCapRetryDelayAtMaxDelayWhenBackoffExceedsIt() {
        final var outbox = mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 10, "lock", Instant.now().plusSeconds(60)));
        final var before = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        outboxSchedulerRepository.retry("lock", List.of(outbox));
        final var after = Instant.now();
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getLockId());
        Assertions.assertFalse(result.getLockUntil().isBefore(before.plusSeconds(10)));
        Assertions.assertFalse(result.getLockUntil().isAfter(after.plusSeconds(10)));
    }

    @Test
    void shouldNotRetryWhenLockIdDiffers() {
        final var lockUntil = Instant.now().truncatedTo(ChronoUnit.MILLIS).plusSeconds(60);
        final var outbox = mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 1, "other", lockUntil));
        outboxSchedulerRepository.retry("lock", List.of(outbox));
        final var result = mongoTemplate.findById(outbox.getId(), Outbox.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("other", result.getLockId());
        Assertions.assertEquals(lockUntil, result.getLockUntil());
    }

    @Test
    void shouldDoNothingWhenRetryingEmptyList() {
        Assertions.assertDoesNotThrow(() -> outboxSchedulerRepository.retry("lock", List.of()));
    }

    @Test
    void shouldNotClaimSameOutboxesForDifferentLockIds() {
        final var timestamp = Instant.now();
        final var firstId = UUID.randomUUID();
        final var secondId = UUID.randomUUID();
        final var thirdId = UUID.randomUUID();
        mongoTemplate.save(new Outbox(firstId, timestamp, "topic", "key", "{}", 0, null, null));
        mongoTemplate.save(new Outbox(secondId, timestamp.plusSeconds(1), "topic", "key", "{}", 0, null, null));
        mongoTemplate.save(new Outbox(thirdId, timestamp.plusSeconds(2), "topic", "key", "{}", 0, null, null));
        final var first = outboxSchedulerRepository.claim("a").stream().map(Outbox::getId).toList();
        final var second = outboxSchedulerRepository.claim("b").stream().map(Outbox::getId).toList();
        Assertions.assertEquals(List.of(firstId, secondId), first);
        Assertions.assertEquals(List.of(thirdId), second);
        Assertions.assertTrue(outboxSchedulerRepository.claim("c").isEmpty());
    }

    @Test
    void shouldReturnLimitedOutboxesInTimestampOrderWhenSomeAreLocked() {
        final var timestamp = Instant.now();
        final var firstId = UUID.randomUUID();
        final var secondId = UUID.randomUUID();
        mongoTemplate.save(new Outbox(UUID.randomUUID(), timestamp, "topic", "key", "{}", 0, "old", Instant.now().plusSeconds(60)));
        mongoTemplate.save(new Outbox(secondId, timestamp.plusSeconds(2), "topic", "key", "{}", 0, null, null));
        mongoTemplate.save(new Outbox(firstId, timestamp.plusSeconds(1), "topic", "key", "{}", 0, null, null));
        mongoTemplate.save(new Outbox(UUID.randomUUID(), timestamp.plusSeconds(3), "topic", "key", "{}", 0, null, null));
        final var result = outboxSchedulerRepository.claim("new");
        Assertions.assertEquals(List.of(firstId, secondId), result.stream().map(Outbox::getId).toList());
        Assertions.assertTrue(result.stream().allMatch(outbox -> outbox.getLockId().equals("new") && outbox.getLockUntil() != null));
    }
}
