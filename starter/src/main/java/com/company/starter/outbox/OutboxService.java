package com.company.starter.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
@Service
public class OutboxService {
    private final JsonMapper json;
    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(final String topic, final String key, final Object data) {
        if (!StringUtils.hasText(topic) || !StringUtils.hasText(key) || data == null) {
            log.atWarn().addKeyValue("topic", topic).addKeyValue("key", key).addKeyValue("data", data == null ? null : data.getClass().getSimpleName()).log("[Outbox] Ignored");
            return;
        }

        outboxRepository.save(new Outbox(topic, key, json.writeValueAsString(data)));
    }
}
