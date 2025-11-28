package com.innowise.paymentservice.service;

import reactor.core.publisher.Mono;

/**
 * @ClassName RandomNumberService
 * @Description Service interface for evaluating random number parity.
 * @Author dshparko
 * @Date 11.11.2025 19:45
 * @Version 1.0
 */
public interface RandomNumberService {

    /**
     * Determines whether a randomly generated number is even.
     *
     * @return Mono emitting true if the number is even, false otherwise
     */
    public Mono<Boolean> isEven();

}
