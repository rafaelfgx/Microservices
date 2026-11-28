package com.company.starter.otp;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OtpInitializer implements ApplicationRunner {
    private static final String INDEX = "otp_timestamp_ttl";
    private final MongoTemplate mongoTemplate;
    private final OtpProperties properties;

    @Override
    public void run(final @NonNull ApplicationArguments arguments) {
        final var operations = mongoTemplate.indexOps(Otp.class);

        final var exists = operations.getIndexInfo().stream().anyMatch(index -> INDEX.equals(index.getName()));

        if (!exists) {
            operations.createIndex(new Index("timestamp", Sort.Direction.ASC).named(INDEX).expire(properties.expiration()));
            return;
        }

        mongoTemplate.getDb().runCommand(new Document("collMod", mongoTemplate.getCollectionName(Otp.class)).append("index", new Document("name", INDEX).append("expireAfterSeconds", properties.expiration().toSeconds())));
    }
}
