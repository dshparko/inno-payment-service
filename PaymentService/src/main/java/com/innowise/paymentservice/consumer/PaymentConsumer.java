package com.innowise.paymentservice.consumer;

import com.innowise.paymentservice.model.dto.OrderEvent;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.dto.PaymentEvent;
import com.innowise.paymentservice.producer.PaymentProducer;
import com.innowise.paymentservice.service.PaymentService;
import com.innowise.paymentservice.service.RandomNumberService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName PaymentConsumer
 * @Description Kafka consumer responsible for handling CREATE_ORDER events.
 * @Author dshparko
 * @Date 09.11.2025 18:13
 * @Version 1.0
 */
@Component
@AllArgsConstructor
public class PaymentConsumer {
    private static final Logger logger = LoggerFactory.getLogger(PaymentConsumer.class);

    private final PaymentService paymentService;
    private final PaymentProducer paymentEventProducer;
    private final RandomNumberService randomNumberService;


    @KafkaListener(topics = "${spring.kafka.topics.create-order}", groupId = "payment-group")
    public void listen(OrderEvent event) {
        logger.info("Received CREATE_ORDER event: {}", event);

        randomNumberService.isEven().subscribe(isEven -> {
            PaymentDto saved = paymentService.processOrderEvent(event, isEven);
            PaymentEvent paymentEvent = paymentService.toPaymentEvent(saved);

            paymentEventProducer.sendCreatePayment(paymentEvent);
            logger.info("Sent CREATE_PAYMENT event: {}", paymentEvent);
        });
    }
}

