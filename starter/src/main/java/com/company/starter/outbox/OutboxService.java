package com.company.starter.outbox;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Service
@Validated
public class OutboxService {
    private final JsonMapper json;
    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(@NotBlank final String topic, @NotBlank final String key, @NotNull @Valid final Object data) {
        outboxRepository.save(new Outbox(topic, key, json.writeValueAsString(data)));
    }
}
