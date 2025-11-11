package com.innowise.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @ClassName WebClientConfig
 * @Description Configuration class for WebClient.
 * Provides a shared, customizable WebClient bean for reactive HTTP communication.
 * @Author dshparko
 * @Date 05.11.2025 17:58
 * @Version 1.0
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

}
