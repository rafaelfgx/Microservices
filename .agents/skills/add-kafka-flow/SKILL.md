---
name: add-kafka-flow
description: Publish or consume Kafka events using the outbox pattern.
---

# Add Kafka Flow

## Publish

1. Create a minimal event record in `<feature>/events/`.
2. Add the domain-to-event mapper method.
3. Publish with `OutboxService.save(topic, key, event)` inside `@Transactional`.
4. Register the topic and `<topic>-dlt` in `.docker/docker-compose.yaml`.

## Consume

1. Create the service-local event record in `<feature>/events/`.
2. Create a `@KafkaListener` delegating to a handler.
3. Implement consumption logic in a handler.
4. Make consumption idempotent.

## Rules

- Topic: `<plural-entity>.<past-tense-action>`.
- Events contain identifiers or minimal data, never full entities.
- Listeners contain no business logic.
- Listener payload: `@Payload @Valid`.
- Kafka JSON uses the repository Jackson 3 serializers.

## Verify

```bash
mvn -f <producer-service> clean verify
mvn -f <consumer-service> clean verify
```
