package com.company.starter.outbox;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
@ToString(exclude = "data")
@CompoundIndexes({
    @CompoundIndex(name = "outbox_unlocked", def = "{'lockExpiration': 1, 'timestamp': 1, '_id': 1}"),
    @CompoundIndex(name = "outbox_expired", def = "{'timestamp': 1, '_id': 1, 'lockExpiration': 1}")
})
@Document("outbox")
public class Outbox {
    @Id
    private UUID id = UUID.randomUUID();

    private Instant timestamp = Instant.now();

    @NotBlank
    @NonNull
    private String topic;

    @NotBlank
    @NonNull
    private String key;

    @NotBlank
    @NonNull
    private String data;

    private int attempts;

    private String lockId;

    private Instant lockExpiration;
}
