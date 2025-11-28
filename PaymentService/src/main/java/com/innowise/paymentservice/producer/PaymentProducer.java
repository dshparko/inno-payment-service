package com.innowise.paymentservice.producer;

import com.innowise.paymentservice.model.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer responsible for publishing {@link PaymentEvent} messages
 * to the CREATE_PAYMENT topic. Used to notify OrderService about payment results.
 *
 * @author dshparko
 * @version 1.1
 * @since 06.11.2025
 */
@Component
public class PaymentProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProducer.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final String createPaymentTopic;

    public PaymentProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate,
                           @Value("${spring.kafka.topics.create-payment}") String createPaymentTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.createPaymentTopic = createPaymentTopic;
    }

    public void sendCreatePayment(PaymentEvent event) {
        if (!isValidEvent(event)) {
            logger.warn("Invalid PaymentEvent: {}", event);
            return;
        }

        kafkaTemplate.send(createPaymentTopic, String.valueOf(event.getPaymentId()), event)
                .whenComplete((result, ex) -> handleSendResult(event, ex));
    }

    private boolean isValidEvent(PaymentEvent event) {
        return event != null && event.getPaymentId() != null && event.getOrderId() != null && event.getStatus() != null;
    }

    private void handleSendResult(PaymentEvent event, Throwable ex) {
        if (ex == null) {
            logger.info("Sent create_payment event [paymentId={}] to topic '{}'",
                    event.getPaymentId(), createPaymentTopic);
        } else {
            logger.error("Failed to send create_payment event [paymentId={}]", event.getPaymentId(), ex);
        }
    }

}