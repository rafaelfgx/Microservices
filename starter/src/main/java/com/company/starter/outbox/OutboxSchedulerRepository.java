package com.company.starter.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class OutboxSchedulerRepository {
    private final OutboxProperties properties;
    private final MongoTemplate mongoTemplate;

    public List<Outbox> claim(final String lockId) {
        final var now = Instant.now();
        final var lockUntil = now.plus(properties.lock());
        final var unlocked = Criteria.where("lockUntil").is(null);
        final var expired = Criteria.where("lockUntil").lte(now);
        final var claimable = new Criteria().orOperator(unlocked, expired);
        final var sort = Sort.by("timestamp").ascending();
        final var claimableQuery = new Query(claimable).with(sort).limit(properties.limit());
        final var claimables = mongoTemplate.find(claimableQuery, Outbox.class);
        if (claimables.isEmpty()) return List.of();
        final var claimableIds = claimables.stream().map(Outbox::getId).toList();
        final var claimQuery = Query.query(Criteria.where("_id").in(claimableIds).andOperator(claimable));
        final var claimUpdate = new Update().set("lockId", lockId).set("lockUntil", lockUntil).inc("attempts", 1);
        mongoTemplate.updateMulti(claimQuery, claimUpdate, Outbox.class);
        final var claimedQuery = Query.query(Criteria.where("_id").in(claimableIds).and("lockId").is(lockId)).with(sort);
        return mongoTemplate.find(claimedQuery, Outbox.class);
    }

    public void retry(final String lockId, final List<Outbox> outboxes) {
        if (outboxes.isEmpty()) return;

        final var bulkOperation = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Outbox.class);

        outboxes.forEach(outbox -> {
            final var query = Query.query(Criteria.where("_id").is(outbox.getId()).and("lockId").is(lockId));
            final var update = new Update().unset("lockId").set("lockUntil", retryDelay(outbox.getAttempts()));
            bulkOperation.updateOne(query, update);
        });

        bulkOperation.execute();
    }

    private Instant retryDelay(final int attempts) {
        final var exponential = properties.retryInitialDelay().toMillis() * Math.pow(properties.retryMultiplier(), Math.max(attempts - 1, 0));
        final var delay = exponential < properties.retryMaxDelay().toMillis() ? Duration.ofMillis((long) exponential) : properties.retryMaxDelay();
        return Instant.now().plus(delay);
    }
}
