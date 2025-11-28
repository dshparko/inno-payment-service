package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.config.RandomNumberClient;
import com.innowise.paymentservice.service.RandomNumberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @ClassName RandomNumberService
 * @Description Implementation of RandomNumberService that determines parity of a random number.
 * @Author dshparko
 * @Date 11.11.2025 19:33
 * @Version 1.0
 */
@Service
@RequiredArgsConstructor
public class RandomNumberServiceImpl implements RandomNumberService {

    private static final Logger logger = LoggerFactory.getLogger(RandomNumberServiceImpl.class);
    private final RandomNumberClient client;

    @Override
    public Mono<Boolean> isEven() {
        return client.fetchRandomNumbers()
                .map(numbers -> {
                    if (numbers == null || numbers.isEmpty()) {
                        logger.warn("Empty response from random number API");
                        return false;
                    }
                    int number = numbers.getFirst();
                    logger.debug("Retrieved random number: {}", number);
                    return number % 2 == 0;
                })
                .defaultIfEmpty(false);
    }
}

