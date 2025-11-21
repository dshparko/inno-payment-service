package com.innowise.paymentservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.PaymentEvent;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableKafka
@ActiveProfiles("test")
class PaymentProducerTest {

    private static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void overrideKafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.topics.create-payment", () -> "create-payment-test");
    }

    @BeforeAll
    static void startKafka() throws Exception {
        KAFKA.start();

        try (AdminClient adminClient = AdminClient.create(
                Collections.singletonMap("bootstrap.servers", KAFKA.getBootstrapServers()))) {
            adminClient.createTopics(Collections.singletonList(
                    new NewTopic("create-payment-test", 1, (short) 1)
            )).all().get();
        }
    }

    @AfterAll
    static void stopKafka() {
        KAFKA.stop();
    }

    @Autowired
    private PaymentProducer paymentProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSendMessageToKafka() throws Exception {
        // given
        PaymentEvent event = new PaymentEvent(1L, 100L, PaymentStatus.SUCCESS);

        // when
        paymentProducer.sendCreatePayment(event);

        // then
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("create-payment-test"));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            assertThat(records.count()).isGreaterThan(0);

            ConsumerRecord<String, String> consumerRecord = records.iterator().next();
            PaymentEvent received = objectMapper.readValue(consumerRecord.value(), PaymentEvent.class);

            assertThat(received.getPaymentId()).isEqualTo(1L);
            assertThat(received.getOrderId()).isEqualTo(100L);
            assertThat(received.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }
    }

    @Test
    void shouldSkipInvalidEvent() {
        PaymentEvent invalidEvent = new PaymentEvent(null, null, null);

        paymentProducer.sendCreatePayment(invalidEvent);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-invalid");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("create-payment-test"));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));
            assertThat(records.count()).isZero();
        }
    }

}
