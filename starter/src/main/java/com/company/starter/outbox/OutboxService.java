package com.company.starter.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Service
public class OutboxService {
    private final JsonMapper json;
    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(final String topic, final String key, final Object data) {
        Assert.hasText(topic, "topic must not be blank");
        Assert.hasText(key, "key must not be blank");
        Assert.notNull(data, "data must not be null");
        outboxRepository.save(new Outbox(topic, key, json.writeValueAsString(data)));
    }
}
