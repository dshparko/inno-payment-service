package com.innowise.paymentservice.config;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @ClassName RandomNumberClient
 * @Description Service for retrieving random numbers from an external API.
 * Used to simulate payment success/failure based on parity.
 * @Author dshparko
 * @Date 05.11.2025 17:55
 * @Version 1.0
 */
@Service
public class RandomNumberClient {

    private static final Logger logger = LoggerFactory.getLogger(RandomNumberClient.class);
    private final WebClient webClient;
    private final String randomApiUrl;

    public RandomNumberClient(WebClient webClient,
                              @Value("${random.api.url}") String randomApiUrl) {
        this.webClient = webClient;
        this.randomApiUrl = randomApiUrl;
    }


    @CircuitBreaker(name = "payment-service", fallbackMethod = "fallbackRandomNumbers")
    public Mono<List<Integer>> fetchRandomNumbers() {
        return webClient.get()
                .uri(randomApiUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    private Mono<List<Integer>> fallbackRandomNumbers(Throwable ex) {
        logger.warn("Fallback triggered for fetchRandomNumbers due to: {}", ex.getMessage());
        return Mono.just(List.of(1));
    }

}