package com.company.starter.outbox;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutboxRepository extends MongoRepository<Outbox, UUID> {
    Page<Outbox> findByTopic(final String topic, final Pageable pageable);

    Page<Outbox> findByKey(final String key, final Pageable pageable);

    Page<Outbox> findByAttemptsGreaterThanEqual(final int attempts, final Pageable pageable);
}
