package com.company.starter.configurations;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

import java.util.HashMap;
import java.util.Map;

@JsonTest
@TestPropertySource(properties = "spring.config.import=classpath:starter.yml")
class JacksonConfigurationTest {
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldEnableExpectedFeaturesWhenObjectMapperIsConfigured() {
        Assertions.assertFalse(objectMapper.isEnabled(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS));
        Assertions.assertFalse(objectMapper.isEnabled(DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS));

        Assertions.assertFalse(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        Assertions.assertTrue(objectMapper.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS));

        Assertions.assertFalse(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assertions.assertTrue(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));
        Assertions.assertTrue(objectMapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assertions.assertTrue(objectMapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        Assertions.assertTrue(objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));

        final var defaultInclusion = objectMapper.serializationConfig().getDefaultPropertyInclusion();
        Assertions.assertEquals(JsonInclude.Include.NON_EMPTY, defaultInclusion.getValueInclusion());
        Assertions.assertEquals(JsonInclude.Include.NON_EMPTY, defaultInclusion.getContentInclusion());
    }

    @Test
    void shouldOmitPropertyWhenValueIsEmpty() {
        final var map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("empty", "");
        final var node = objectMapper.readTree(objectMapper.writeValueAsString(map));
        Assertions.assertEquals("value", node.get("key").asString());
        Assertions.assertFalse(node.has("empty"));
    }

    @Test
    void shouldReturnNullWhenDeserializingEmptyString() {
        final var json = objectMapper.writeValueAsString("");
        Assertions.assertNull(objectMapper.readValue(json, Map.class));
    }
}
