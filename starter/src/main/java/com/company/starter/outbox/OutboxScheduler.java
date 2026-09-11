package com.company.starter.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxScheduler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final OutboxSchedulerRepository outboxSchedulerRepository;

    @Scheduled(fixedDelayString = "${outbox.delay}")
    public void handle() {
        final var lockId = UUID.randomUUID().toString();

        try {
            log.atDebug().addKeyValue("lockId", lockId).log("[Outbox] Starting");
            final var outboxes = outboxSchedulerRepository.claim(lockId);
            if (outboxes.isEmpty()) return;
            final var futures = outboxes.stream().map(this::each).toList();
            final var sent = futures.stream().map(future -> future.exceptionally(throwable -> Optional.empty()).join()).flatMap(Optional::stream).collect(Collectors.toSet());
            if (!sent.isEmpty()) outboxRepository.deleteAllById(sent);
            final var failed = outboxes.stream().filter(outbox -> !sent.contains(outbox.getId())).toList();
            outboxSchedulerRepository.retry(lockId, failed);
            log.atDebug().addKeyValue("lockId", lockId).addKeyValue("sent", sent.size()).addKeyValue("failed", failed.size()).log("[Outbox] Finished");
        } catch (final Exception exception) {
            log.atError().setCause(exception).addKeyValue("lockId", lockId).log("[Outbox] Error");
        }
    }

    private CompletableFuture<Optional<UUID>> each(final Outbox outbox) {
        try {
            log.atDebug().addKeyValue("outbox", outbox).log("[Outbox] [Each] Starting");
            return kafkaTemplate.send(outbox.getTopic(), outbox.getKey(), outbox.getData()).handle((_, throwable) -> handle(outbox, throwable));
        } catch (final Exception exception) {
            return CompletableFuture.completedFuture(handle(outbox, exception));
        }
    }

    private Optional<UUID> handle(final Outbox outbox, final Throwable throwable) {
        return throwable == null ? handleSuccess(outbox) : handleError(outbox, throwable);
    }

    private Optional<UUID> handleSuccess(final Outbox outbox) {
        log.atDebug().addKeyValue("outbox", outbox).log("[Outbox] [Each] Success");
        return Optional.of(outbox.getId());
    }

    private Optional<UUID> handleError(final Outbox outbox, final Throwable throwable) {
        log.atError().setCause(throwable).addKeyValue("outbox", outbox).log("[Outbox] [Each] Error");
        return Optional.empty();
    }
}
