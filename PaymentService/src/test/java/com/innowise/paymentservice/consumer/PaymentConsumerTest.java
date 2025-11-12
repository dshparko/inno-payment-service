package com.innowise.paymentservice.consumer;


import com.innowise.paymentservice.model.dto.OrderEvent;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.dto.PaymentEvent;
import com.innowise.paymentservice.producer.PaymentProducer;
import com.innowise.paymentservice.service.PaymentService;
import com.innowise.paymentservice.service.RandomNumberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = "create-order")
class PaymentConsumerTest {

    @DynamicPropertySource
    static void setMongoUri(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27017/test-db");
    }

    @Autowired
    private PaymentConsumer paymentConsumer;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private PaymentProducer paymentProducer;

    @MockitoBean
    private RandomNumberService randomNumberService;

    @Test
    void shouldProcessOrderEventAndSendPaymentEvent() {
        // given
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(123L);
        orderEvent.setUserId(456L);

        PaymentDto paymentDto = PaymentDto.builder()
                .orderId(123L)
                .userId(456L)
                .timestamp(Instant.now())
                .build();

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setOrderId(123L);
        paymentEvent.setPaymentId(456L);

        when(randomNumberService.isEven()).thenReturn(Mono.just(true));
        when(paymentService.processOrderEvent(orderEvent, true)).thenReturn(paymentDto);
        when(paymentService.toPaymentEvent(paymentDto)).thenReturn(paymentEvent);

        // when
        paymentConsumer.listen(orderEvent);

        // then
        verify(randomNumberService, times(1)).isEven();
        verify(paymentService, times(1)).processOrderEvent(orderEvent, true);
        verify(paymentService, times(1)).toPaymentEvent(paymentDto);
        verify(paymentProducer, times(1)).sendCreatePayment(paymentEvent);
    }
}
