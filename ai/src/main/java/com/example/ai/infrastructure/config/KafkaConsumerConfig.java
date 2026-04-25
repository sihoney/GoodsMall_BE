package com.example.ai.infrastructure.config;

import com.example.ai.infrastructure.messaging.kafka.InvalidProductEventPayloadException;
import com.example.ai.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.ai.infrastructure.messaging.kafka.KafkaTopics;
import com.example.ai.infrastructure.messaging.kafka.ProductEventParseException;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * AI 모듈 Kafka consumer 설정.
 * <p>
 * Product 이벤트는 payload를 문자열로 받은 뒤 내부 ObjectMapper로 직접 파싱하므로
 * StringDeserializer 기반 기본 listener factory를 명시적으로 등록한다.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> productEventConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConsumerGroups.AI_PRODUCT_EMBEDDING_GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public DefaultErrorHandler productEventKafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${ai.kafka.retry.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${ai.kafka.retry.max-attempts:3}") int maxAttempts,
            @Value("${ai.kafka.retry.multiplier:2.0}") double multiplier,
            @Value("${ai.kafka.retry.max-interval-ms:5000}") long maxIntervalMs
    ) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxAttempts);
        backOff.setInitialInterval(initialIntervalMs);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxIntervalMs);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(KafkaTopics.PRODUCT_EVENT_DLQ, record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(
                ProductEventParseException.class,
                InvalidProductEventPayloadException.class
        );
        return errorHandler;
    }

    @Bean(name = "productEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> productEventKafkaListenerContainerFactory(
            ConsumerFactory<String, String> productEventConsumerFactory,
            DefaultErrorHandler productEventKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productEventConsumerFactory);
        factory.setCommonErrorHandler(productEventKafkaErrorHandler);
        return factory;
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactory<String, String> productEventKafkaListenerContainerFactory
    ) {
        return productEventKafkaListenerContainerFactory;
    }
}
