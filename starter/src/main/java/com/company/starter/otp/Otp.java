package com.company.starter.otp;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@ToString(exclude = "hash")
@CompoundIndex(name = "otp_type_key", def = "{'type': 1, 'key': 1}", unique = true)
@Document("otp")
public class Otp {
    @Id
    private UUID id;

    private Instant timestamp;

    private String type;

    private String key;

    private String hash;

    private int attempts;
}
