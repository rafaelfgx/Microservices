package com.company.starter.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutboxRepository extends MongoRepository<Outbox, UUID> {
}
