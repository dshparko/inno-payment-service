package com.innowise.paymentservice.config;

import com.innowise.paymentservice.model.dto.OrderEvent;
import com.innowise.paymentservice.model.dto.PaymentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final String AUTO_OFFSET_RESET = "earliest";

    @Bean
    public ProducerFactory<String, OrderEvent> orderEventProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs(bootstrapServers));
    }

    @Bean
    public KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate(
            ProducerFactory<String, OrderEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, PaymentEvent> paymentEventProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs(bootstrapServers));
    }

    @Bean
    public KafkaTemplate<String, PaymentEvent> paymentEventKafkaTemplate(
            ProducerFactory<String, PaymentEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> commonProducerConfigs(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderEventKafkaListenerContainerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            KafkaTemplate<String, OrderEvent> kafkaTemplate) {

        JsonDeserializer<OrderEvent> deserializer = new JsonDeserializer<>(OrderEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(true);
        deserializer.setUseTypeMapperForKey(false);

        DefaultKafkaConsumerFactory<String, OrderEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(commonConsumerConfigs(bootstrapServers, groupId),
                        new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(1000L, 3)
        );
        errorHandler.setAckAfterHandle(false);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    private Map<String, Object> commonConsumerConfigs(String bootstrapServers, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        return props;
    }
}
