package com.company.configurationservice.configuration;

import com.company.configurationservice.shared.Data;
import com.company.configurationservice.shared.SpringBootTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@AutoConfigureMockMvc(addFilters = false)
@Import(SpringBootTestConfiguration.class)
@SpringBootTest
class ConfigurationIntegrationTest {
    @Autowired
    MockMvcTester mvc;

    @Autowired
    JsonMapper json;

    @Autowired
    MongoTemplate mongo;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @Test
    void shouldReturnNotFoundWhenNoConfigurationsExist() {
        final var result = mvc.get().uri("/configurations").exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnOkWhenConfigurationsExist() {
        mongo.save(Data.CONFIGURATION);
        final var result = mvc.get().uri("/configurations").exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().isEqualTo(json.writeValueAsString(List.of(Data.CONFIGURATION_RESPONSE)), JsonCompareMode.STRICT);
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NON_EXISTENT_ID, Data.ID})
    void shouldReturnNotFoundWhenConfigurationDoesNotExist(final String id) {
        final var result = mvc.get().uri("/configurations/{id}", id).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnOkWhenConfigurationExists() {
        mongo.save(Data.CONFIGURATION);
        final var result = mvc.get().uri("/configurations/{id}", Data.ID).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().isEqualTo(json.writeValueAsString(Data.CONFIGURATION_RESPONSE), JsonCompareMode.STRICT);
    }

    @Test
    void shouldReturnCreatedWhenCreatingConfiguration() {
        final var result = mvc.post().uri("/configurations").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Data.CREATE_CONFIGURATION_REQUEST)).exchange();
        final var savedConfiguration = mongo.findById(Data.ID, Configuration.class);
        Assertions.assertThat(result).hasStatus(HttpStatus.CREATED);
        Assertions.assertThat(savedConfiguration).isNotNull();
        Assertions.assertThat(savedConfiguration.getId()).isEqualTo(Data.ID);
        Assertions.assertThat(savedConfiguration.getValue()).isEqualTo(Data.VALUE);
        Assertions.assertThat(savedConfiguration.getDescription()).isEqualTo(Data.DESCRIPTION);
        Assertions.assertThat(savedConfiguration.getGroup()).isEqualTo(Data.GROUP);
    }

    @Test
    void shouldReturnNoContentWhenUpdatingConfiguration() {
        mongo.save(Data.CONFIGURATION);
        final var result = mvc.put().uri("/configurations/{id}", Data.ID).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Data.UPDATE_CONFIGURATION_REQUEST)).exchange();
        final var savedConfiguration = mongo.findById(Data.ID, Configuration.class);
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(savedConfiguration).isNotNull();
        Assertions.assertThat(savedConfiguration.getId()).isEqualTo(Data.ID);
        Assertions.assertThat(savedConfiguration.getValue()).isEqualTo(Data.UPDATED_VALUE);
        Assertions.assertThat(savedConfiguration.getDescription()).isEqualTo(Data.UPDATED_DESCRIPTION);
        Assertions.assertThat(savedConfiguration.getGroup()).isEqualTo(Data.UPDATED_GROUP);
    }

    @Test
    void shouldReturnNoContentWhenUpdatingValue() {
        mongo.save(Data.CONFIGURATION);
        final var result = mvc.patch().uri("/configurations/{id}/value/{value}", Data.ID, Data.UPDATED_VALUE).contentType(MediaType.APPLICATION_JSON).exchange();
        final var savedConfiguration = mongo.findById(Data.ID, Configuration.class);
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(savedConfiguration).isNotNull();
        Assertions.assertThat(savedConfiguration.getValue()).isEqualTo(Data.UPDATED_VALUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NON_EXISTENT_ID, Data.ID})
    void shouldReturnNoContentWhenDeletingConfiguration(final String id) {
        mongo.save(Data.CONFIGURATION);
        final var result = mvc.delete().uri("/configurations/{id}", id).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(mongo.findById(id, Configuration.class)).isNull();
    }
}
