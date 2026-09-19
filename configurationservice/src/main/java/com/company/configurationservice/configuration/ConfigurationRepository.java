package com.company.configurationservice.configuration;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends MongoRepository<Configuration, String> {
    <T> List<T> findAllBy(final Class<T> type);

    <T> Optional<T> findById(final String id, final Class<T> type);
}
