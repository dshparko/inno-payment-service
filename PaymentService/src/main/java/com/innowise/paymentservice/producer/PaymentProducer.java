package com.innowise.paymentservice.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.paymentservice.model.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer responsible for publishing {@link PaymentEvent} messages
 * to the CREATE_PAYMENT topic. Used to notify OrderService about payment results.
 *
 * @author dshparko
 * @version 1.1
 * @since 06.11.2025
 */
@Service
public class PaymentProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String createPaymentTopic;

    public PaymentProducer(KafkaTemplate<String, String> kafkaTemplate,
                           ObjectMapper objectMapper,
                           @Value("${spring.kafka.topics.create-payment}") String createPaymentTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.createPaymentTopic = createPaymentTopic;
    }

    public void sendCreatePayment(PaymentEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(createPaymentTopic, payload);
            logger.info("Sent CREATE_PAYMENT event to topic '{}': {}", createPaymentTopic, payload);
        } catch (JsonProcessingException e) {
            String context = String.format("Failed to serialize PaymentEvent for orderId=%s, userId=%s, status=%s",
                    event.getOrderId(), event.getPaymentId(), event.getStatus());
            throw new IllegalStateException(context, e);
        }
    }

}