---
name: add-kafka-flow
description: Publish or consume Kafka events between services using the outbox pattern. Use when adding asynchronous communication or a new event flow.
---

# Add Kafka Flow

## Publish (producer side)

1. Create the event record in `<feature>/events/`:

    ```java
    public record OrderCreatedEvent(UUID id) {
    }
    ```

2. Add a mapper method converting the domain entity to the event.

3. Publish through `OutboxService` inside a transactional handler (outbox guarantees atomicity between state change and event):

    ```java
    @Transactional
    @Override
    public ResponseEntity<UUID> handle(final CreateOrderRequest request) {
        final var order = orderRepository.save(orderMapper.toOrder(request));
        outboxService.save("orders.created", order.getId().toString(), orderMapper.toCreatedEvent(order));
        return ResponseEntity.status(HttpStatus.CREATED).body(order.getId());
    }
    ```

## Consume (consumer side)

1. Create the event record in `<feature>/events/` (duplicate the schema; services do not share event classes).

2. Create the listener in `<feature>/listeners/`, delegating to a handler:

    ```java
    @RequiredArgsConstructor
    @Component
    public class PaymentApprovedEventListener {
        private final PaymentApprovedEventHandler paymentApprovedEventHandler;

        @KafkaListener(topics = "payments.approved")
        public void listen(final PaymentApprovedEvent event) {
            paymentApprovedEventHandler.handle(event);
        }
    }
    ```

3. Create the handler in `<feature>/handlers/` with the consumption logic.

## Topic Registration

Add the topic to the Kafka bootstrap command in `.docker/docker-compose.yaml`:

```
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9094 --create --if-not-exists --topic <topic>;
```

## Conventions

- Topic naming: `<plural-entity>.<past-tense-action>` (e.g. `orders.created`, `payments.approved`, `payments.canceled`)
- Events are minimal records carrying identifiers, not full entities
- Listeners never contain logic; they only delegate to handlers
- Reference implementation: `orderservice` and `paymentservice`

## Verify

```bash
mvn -f <producer-service> clean verify
mvn -f <consumer-service> clean verify
```
