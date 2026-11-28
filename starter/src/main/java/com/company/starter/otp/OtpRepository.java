package com.company.starter.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class OtpRepository {
    private final MongoTemplate mongoTemplate;

    public void upsert(final String type, final String key, final String hash) {
        final var update = new Update().setOnInsert("_id", UUID.randomUUID()).set("timestamp", Instant.now()).set("hash", hash).set("attempts", 0);
        mongoTemplate.upsert(query(type, key), update, Otp.class);
    }

    public Optional<Otp> consume(final String type, final String key, final Instant oldest, final int maximumAttempts) {
        final var query = query(type, key).addCriteria(Criteria.where("timestamp").gt(oldest).and("attempts").lt(maximumAttempts));
        return Optional.ofNullable(mongoTemplate.findAndModify(query, new Update().inc("attempts", 1), Otp.class));
    }

    public boolean delete(final UUID id) {
        return mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), Otp.class).getDeletedCount() > 0;
    }

    private Query query(final String type, final String key) {
        return Query.query(Criteria.where("type").is(type).and("key").is(key));
    }
}
