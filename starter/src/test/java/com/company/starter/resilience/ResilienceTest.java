package com.company.starter.resilience;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(classes = {
    ResilienceConfiguration.class,
    DefaultRetryable.class,
    ResilienceService.class
})
@TestPropertySource(properties = "spring.config.import=classpath:starter.yml")
class ResilienceTest {
    @MockitoSpyBean
    ResilienceService resilienceService;

    @Test
    void shouldRetryThreeTimesWhenRetryableExceptionIsThrown() throws Exception {
        Assertions.assertThrows(Exception.class, () -> resilienceService.retryable());
        Mockito.verify(resilienceService, Mockito.times(3)).retryable();
    }

    @Test
    void shouldNotRetryWhenNonRetryableExceptionIsThrown() {
        Assertions.assertThrows(Exception.class, () -> resilienceService.nonRetryable());
        Mockito.verify(resilienceService, Mockito.times(1)).nonRetryable();
    }
}
