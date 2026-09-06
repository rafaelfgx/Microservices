package com.company.starter.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Service
public class OutboxService {
    private final JsonMapper json;
    private final OutboxRepository outboxRepository;

    public void save(final String topic, final String key, final Object data) {
        outboxRepository.save(new Outbox(topic, key, json.writeValueAsString(data)));
    }
}
