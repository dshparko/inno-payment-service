package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class PaymentRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        payment = new Payment();
        payment.setOrderId(123L);
        payment.setUserId(456L);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(Instant.now());
        payment.setPaymentAmount(BigDecimal.valueOf(99.99));

        paymentRepository.save(payment);
    }

    @Test
    void shouldFindByOrderId() {
        List<Payment> result = paymentRepository.findByOrderId(123L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(123L);
    }

    @Test
    void shouldFindByUserId() {
        List<Payment> result = paymentRepository.findByUserId(456L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(456L);
    }

    @Test
    void shouldFindByStatusIn() {
        List<Payment> result = paymentRepository.findByStatusIn(List.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void shouldFindByTimestampBetween() {
        Instant now = Instant.now();
        List<Payment> result = paymentRepository.findByTimestampBetween(now.minusSeconds(60), now.plusSeconds(60));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimestamp()).isBetween(now.minusSeconds(60), now.plusSeconds(60));
    }

    @Test
    void shouldFindAmountsByTimestampBetween() {
        Instant now = Instant.now();
        List<Payment> result = paymentRepository.findAmountsByTimestampBetween(now.minusSeconds(60), now.plusSeconds(60));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }
}
