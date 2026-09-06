package com.company.starter.outbox;

import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends MongoRepository<Outbox, UUID> {
    List<Outbox> findByAttemptsLessThanOrderByTimestampAsc(final int attempts, final Limit limit);
}
