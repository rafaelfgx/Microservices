package com.company.starter.resilience;

import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Service
public class ResilienceService {
    @DefaultRetryable
    public void retryable() throws ConnectException {
        throw new ConnectException();
    }

    @DefaultRetryable
    public void nonRetryable() throws IllegalStateException {
        throw new IllegalStateException();
    }
}
