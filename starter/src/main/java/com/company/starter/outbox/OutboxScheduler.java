package com.company.starter.outbox;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CustomLog
@RequiredArgsConstructor
@Component
public class OutboxScheduler {
    private final OutboxProperties properties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final OutboxSchedulerRepository outboxSchedulerRepository;

    @Scheduled(fixedDelayString = "${outbox.delay}")
    public void schedule() {
        final var lockId = UUID.randomUUID().toString();

        try {
            final var outboxes = outboxSchedulerRepository.claim(lockId);
            if (outboxes.isEmpty()) return;
            final var futures = outboxes.stream().map(this::each).toList();
            final var sent = futures.stream().map(CompletableFuture::join).flatMap(Optional::stream).collect(Collectors.toSet());
            if (!sent.isEmpty()) outboxRepository.deleteAllById(sent);
            final var failed = outboxes.stream().filter(outbox -> !sent.contains(outbox.getId())).toList();
            log.at().info().marker("Outbox").message("Schedule").attribute("lockId", lockId).attribute("sent", sent.size()).attribute("failed", failed.size()).log();
        } catch (final Exception exception) {
            log.at().error().marker("Outbox").message("Error").exception(exception).attribute("lockId", lockId).log();
        }
    }

    private CompletableFuture<Optional<UUID>> each(final Outbox outbox) {
        try {
            return kafkaTemplate
                .send(outbox.getTopic(), outbox.getKey(), outbox.getData())
                .orTimeout(properties.lock().toMillis(), TimeUnit.MILLISECONDS)
                .handle((_, throwable) -> throwable == null ? Optional.of(outbox.getId()) : error(outbox, throwable));
        } catch (final Exception exception) {
            return CompletableFuture.completedFuture(error(outbox, exception));
        }
    }

    private Optional<UUID> error(final Outbox outbox, final Throwable throwable) {
        log.at().error().marker("Outbox").message("Error").exception(throwable).attribute("outbox", outbox).log();
        return Optional.empty();
    }
}
