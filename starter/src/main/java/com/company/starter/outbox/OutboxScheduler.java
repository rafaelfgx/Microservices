package com.company.starter.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxScheduler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final OutboxProperties outboxProperties;

    @SchedulerLock(name = "outbox", lockAtMostFor = "${outbox.lock}")
    @Scheduled(fixedDelayString = "${outbox.delay}")
    public void handle() {
        try {
            log.atDebug().log("[Outbox] Starting");
            final var outboxes = outboxRepository.findByAttemptsLessThanOrderByTimestampAsc(outboxProperties.attempts(), Limit.of(outboxProperties.limit()));
            final var futures = outboxes.stream().map(this::each).toList();
            final var sentIds = futures.stream().map(future -> future.exceptionally(throwable -> Optional.empty()).join()).flatMap(Optional::stream).toList();
            if (!sentIds.isEmpty()) outboxRepository.deleteAllById(sentIds);
            log.atDebug().log("[Outbox] Finished");
        } catch (final Exception exception) {
            log.atError().setCause(exception).log("[Outbox] Error");
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
        outbox.incrementAttempts();
        outboxRepository.save(outbox);
        return Optional.empty();
    }
}
