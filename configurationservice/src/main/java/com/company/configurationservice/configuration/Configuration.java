package com.company.configurationservice.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
@Setter(AccessLevel.PRIVATE)
@Document("configurations")
public class Configuration {
    @Id
    private String id;

    private Object value;

    private String description;

    private String group;

    public Configuration updateValue(final Object value) {
        this.value = value;
        return this;
    }
}
