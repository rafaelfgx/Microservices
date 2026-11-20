package com.company.starter.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class OutboxSchedulerRepository {
    private final OutboxProperties properties;
    private final MongoTemplate mongoTemplate;

    public List<Outbox> claim(final String lockId) {
        final var unlocked = Criteria.where("lockExpiration").is(null);
        final var expired = Criteria.where("lockExpiration").lte(Instant.now());
        final var claimable = new Criteria().orOperator(unlocked, expired);
        final var sort = Sort.by("timestamp").ascending().and(Sort.by("_id").ascending());
        final var claimableQuery = new Query(claimable).with(sort).limit(properties.limit());
        final var claimables = mongoTemplate.find(claimableQuery, Outbox.class);
        if (claimables.isEmpty()) return List.of();
        final var claimableIds = claimables.stream().map(Outbox::getId).toList();
        final var claimQuery = Query.query(Criteria.where("_id").in(claimableIds).andOperator(claimable));
        final var claimUpdate = new Update().set("lockId", lockId).set("lockExpiration", Instant.now().plus(properties.lock())).inc("attempts", 1);
        mongoTemplate.updateMulti(claimQuery, claimUpdate, Outbox.class);
        final var claimedQuery = Query.query(Criteria.where("_id").in(claimableIds).and("lockId").is(lockId)).with(sort);
        return mongoTemplate.find(claimedQuery, Outbox.class);
    }
}
