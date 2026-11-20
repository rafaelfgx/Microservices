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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@DataMongoTest
@TestPropertySource(properties = {
    "outbox.delay=PT1S",
    "outbox.lock=PT2S",
    "outbox.limit=2"
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
        Assertions.assertNotNull(result.getFirst().getLockExpiration());
        Assertions.assertEquals(1, result.getFirst().getAttempts());
    }

    @Test
    void shouldReturnNonEmptyWhenLockExpired() {
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 0, "old", Instant.now().minusSeconds(7200)));
        final var result = outboxSchedulerRepository.claim("new");
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("new", result.getFirst().getLockId());
        Assertions.assertEquals(1, result.getFirst().getAttempts());
    }

    @Test
    void shouldReturnEmptyWhenLocked() {
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 0, "old", Instant.now().plusSeconds(60)));
        Assertions.assertTrue(outboxSchedulerRepository.claim("new").isEmpty());
    }

    @Test
    void shouldKeepActiveLockWhenClaimingOtherOutboxes() {
        final var lockedOutbox = mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "locked", "key", "{}", 3, "old", Instant.now().plusSeconds(60)));
        mongoTemplate.save(new Outbox("claimable", "key", "{}"));
        final var result = outboxSchedulerRepository.claim("new");
        final var lockedResult = mongoTemplate.findById(lockedOutbox.getId(), Outbox.class);
        Assertions.assertEquals(1, result.size());
        Assertions.assertNotNull(lockedResult);
        Assertions.assertEquals("old", lockedResult.getLockId());
        Assertions.assertEquals(3, lockedResult.getAttempts());
    }

    @Test
    void shouldClaimDistinctOutboxesForDifferentLockIds() {
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
        Assertions.assertTrue(result.stream().allMatch(outbox -> outbox.getLockId().equals("new") && outbox.getLockExpiration() != null));
    }

    @Test
    void shouldIncrementAttemptsWhenClaimingOutboxAgainAfterLockExpires() {
        final var outbox = mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 1, "old", Instant.now().minusSeconds(1)));
        final var firstClaim = outboxSchedulerRepository.claim("first");
        mongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").is(outbox.getId())),
            new Update().set("lockExpiration", Instant.EPOCH),
            Outbox.class);

        final var secondClaim = outboxSchedulerRepository.claim("second");

        Assertions.assertEquals(1, firstClaim.size());
        Assertions.assertEquals(1, secondClaim.size());
        Assertions.assertEquals("second", secondClaim.getFirst().getLockId());
        Assertions.assertEquals(3, secondClaim.getFirst().getAttempts());
    }
}
