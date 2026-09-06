package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import tools.jackson.databind.json.JsonMapper;

@DataMongoTest
@Import({
    MongoTestConfiguration.class,
    OutboxService.class,
    OutboxServiceTest.OutboxServiceTestConfiguration.class
})
class OutboxServiceTest {
    private static final String TOPIC = "topic";
    private static final String KEY = "key";

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    OutboxService outboxService;

    @BeforeEach
    void beforeEach() {
        mongoTemplate.dropCollection(Outbox.class);
    }

    @Test
    void shouldSaveOutboxWhenDataIsNull() {
        final var result = saveAndGetSingleOutbox(null);
        Assertions.assertEquals(TOPIC, result.getTopic());
        Assertions.assertEquals(KEY, result.getKey());
        Assertions.assertEquals("null", result.getData());
    }

    @Test
    void shouldSaveOutboxWhenDataIsObject() {
        final var result = saveAndGetSingleOutbox("value");
        Assertions.assertEquals(TOPIC, result.getTopic());
        Assertions.assertEquals(KEY, result.getKey());
        Assertions.assertEquals("\"value\"", result.getData());
    }

    private Outbox saveAndGetSingleOutbox(final Object data) {
        outboxService.save(TOPIC, KEY, data);
        final var outboxes = mongoTemplate.findAll(Outbox.class);
        Assertions.assertEquals(1, outboxes.size());
        return outboxes.getFirst();
    }

    @TestConfiguration
    static class OutboxServiceTestConfiguration {
        @Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }
    }
}
