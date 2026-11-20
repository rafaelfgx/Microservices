package com.company.starter.outbox;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Outbox")
@RequestMapping("/outbox")
@RequiredArgsConstructor
@RestController
public class OutboxController {
    private final OutboxRepository outboxRepository;

    @GetMapping
    public Page<Outbox> findAll(@ParameterObject @Valid final Pageable pageable) {
        return outboxRepository.findAll(pageable);
    }

    @GetMapping("{id}")
    public ResponseEntity<Outbox> findById(@PathVariable final UUID id) {
        return outboxRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("topics/{topic}")
    public Page<Outbox> findByTopic(@PathVariable final String topic, @ParameterObject @Valid final Pageable pageable) {
        return outboxRepository.findByTopic(topic, pageable);
    }

    @GetMapping("keys/{key}")
    public Page<Outbox> findByKey(@PathVariable final String key, @ParameterObject @Valid final Pageable pageable) {
        return outboxRepository.findByKey(key, pageable);
    }

    @GetMapping("attempts/{attempts}")
    public Page<Outbox> findByAttempts(@PathVariable final int attempts, @ParameterObject @Valid final Pageable pageable) {
        return outboxRepository.findByAttemptsGreaterThanEqual(attempts, pageable);
    }

    @PostMapping
    public Outbox save(@Valid @RequestBody final Outbox outbox) {
        return outboxRepository.save(outbox);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable final UUID id) {
        outboxRepository.deleteById(id);
    }
}
