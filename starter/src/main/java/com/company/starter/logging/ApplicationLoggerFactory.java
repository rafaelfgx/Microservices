package com.company.starter.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationLoggerFactory {
    public static ApplicationLogger getLogger(Class<?> type) {
        return new Slf4jApplicationLogger(type);
    }
}
