package com.innowise.paymentservice.service;


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

    private final WebClient webClient;
    private final String randomApiUrl;

    public RandomNumberClient(WebClient webClient,
                              @Value("${random.api.url}") String randomApiUrl) {
        this.webClient = webClient;
        this.randomApiUrl = randomApiUrl;
    }


    public Mono<List<Integer>> fetchRandomNumbers() {
        return webClient.get()
                .uri(randomApiUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

}


