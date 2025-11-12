package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.config.RandomNumberClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RandomNumberServiceImplTest {

    private RandomNumberClient client;
    private RandomNumberServiceImpl service;

    @BeforeEach
    void setUp() {
        client = mock(RandomNumberClient.class);
        service = new RandomNumberServiceImpl(client);
    }

    @Test
    void isEven_shouldReturnTrue_whenNumberIsEven() {
        when(client.fetchRandomNumbers()).thenReturn(Mono.just(List.of(42)));

        StepVerifier.create(service.isEven())
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isEven_shouldReturnFalse_whenNumberIsOdd() {
        when(client.fetchRandomNumbers()).thenReturn(Mono.just(List.of(17)));

        StepVerifier.create(service.isEven())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isEven_shouldReturnFalse_whenListIsEmpty() {
        when(client.fetchRandomNumbers()).thenReturn(Mono.just(Collections.emptyList()));

        StepVerifier.create(service.isEven())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isEven_shouldReturnFalse_whenMonoIsEmpty() {
        when(client.fetchRandomNumbers()).thenReturn(Mono.empty());

        StepVerifier.create(service.isEven())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isEven_shouldReturnFalse_onError() {
        when(client.fetchRandomNumbers()).thenReturn(Mono.error(new RuntimeException("API error")));

        StepVerifier.create(service.isEven())
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("API error"))
                .verify();
    }
}
