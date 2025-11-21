package com.innowise.paymentservice.consumer;


import com.innowise.paymentservice.model.PaymentStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1, topics = "create-order")
@ActiveProfiles("test")
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
        orderEvent.setAmount(BigDecimal.TEN);

        PaymentDto paymentDto = PaymentDto.builder()
                .paymentId("PAYMENT-1")
                .orderId(123L)
                .userId(456L)
                .status(PaymentStatus.SUCCESS)
                .paymentAmount(BigDecimal.TEN)
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

    @Test
    void shouldSkipDuplicateOrderEvent() {
        // given
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(999L);
        orderEvent.setUserId(111L);
        orderEvent.setAmount(BigDecimal.ONE);

        when(paymentService.isAlreadyProcessed(orderEvent.getOrderId())).thenReturn(true);

        // when
        paymentConsumer.listen(orderEvent);

        // then: остальные методы НЕ вызываются
        verify(paymentService, times(1)).isAlreadyProcessed(orderEvent.getOrderId());
        verify(paymentService, times(0)).processOrderEvent(any(), anyBoolean());
        verify(paymentService, times(0)).toPaymentEvent(any());
        verify(paymentProducer, times(0)).sendCreatePayment(any());
    }

}
