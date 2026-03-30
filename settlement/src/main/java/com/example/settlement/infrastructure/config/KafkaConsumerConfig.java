package com.example.settlement.infrastructure.config;

import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
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
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

/**
 * settlement 모듈 Kafka consumer(소비기) 설정을 담당한다.
 * <p>
 * 정산 원천/지급 결과 소비 경로에 retry(재시도)/backoff(백오프)/DLQ(사후처리큐) 정책을 적용한다.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, SettlementCandidateCreatedMessage> settlementCandidateCreatedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${settlement.kafka.consumer-groups.settlement-candidate-created:settlement-service}") String groupId
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 정산 원천 이벤트 소비 전용 리스너 팩토리를 생성한다.
     * <p>
     * 예외 발생 시 공통 에러 처리기로 재시도 후 DLQ 발행을 수행한다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SettlementCandidateCreatedMessage>
        settlementCandidateCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, SettlementCandidateCreatedMessage> settlementCandidateCreatedConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${settlement.kafka.retry.settlement-candidate-created.dlq-topic:payment.settlement-candidate-created.dlq}") String dlqTopic,
            @Value("${settlement.kafka.retry.settlement-candidate-created.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${settlement.kafka.retry.settlement-candidate-created.multiplier:2.0}") double multiplier,
            @Value("${settlement.kafka.retry.settlement-candidate-created.max-interval-ms:10000}") long maxIntervalMs,
            @Value("${settlement.kafka.retry.settlement-candidate-created.max-retries:3}") int maxRetries
    ) {
        ConcurrentKafkaListenerContainerFactory<String, SettlementCandidateCreatedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(settlementCandidateCreatedConsumerFactory);
        factory.setCommonErrorHandler(createCommonErrorHandler(
                kafkaTemplate,
                dlqTopic,
                initialIntervalMs,
                multiplier,
                maxIntervalMs,
                maxRetries
        ));
        return factory;
    }

    @Bean
    public ConsumerFactory<String, SellerSettlementPayoutResultMessage> sellerSettlementPayoutResultConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${settlement.kafka.consumer-groups.settlement-payout-result:settlement-service}") String groupId
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 정산 지급 결과 이벤트 소비 전용 리스너 팩토리를 생성한다.
     * <p>
     * 예외 발생 시 공통 에러 처리기로 재시도 후 DLQ 발행을 수행한다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SellerSettlementPayoutResultMessage>
        sellerSettlementPayoutResultKafkaListenerContainerFactory(
            ConsumerFactory<String, SellerSettlementPayoutResultMessage> sellerSettlementPayoutResultConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${settlement.kafka.retry.settlement-payout-result.dlq-topic:payment.seller-payout-result.dlq}") String dlqTopic,
            @Value("${settlement.kafka.retry.settlement-payout-result.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${settlement.kafka.retry.settlement-payout-result.multiplier:2.0}") double multiplier,
            @Value("${settlement.kafka.retry.settlement-payout-result.max-interval-ms:10000}") long maxIntervalMs,
            @Value("${settlement.kafka.retry.settlement-payout-result.max-retries:3}") int maxRetries
    ) {
        ConcurrentKafkaListenerContainerFactory<String, SellerSettlementPayoutResultMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sellerSettlementPayoutResultConsumerFactory);
        factory.setCommonErrorHandler(createCommonErrorHandler(
                kafkaTemplate,
                dlqTopic,
                initialIntervalMs,
                multiplier,
                maxIntervalMs,
                maxRetries
        ));
        return factory;
    }

    /**
     * 공통 Kafka 소비 에러 처리기를 생성한다.
     * <p>
     * - RETRYABLE 예외: 지수 백오프 재시도
     * - 재시도 소진: DLQ 토픽으로 발행
     * - IllegalArgumentException: 비재시도 예외로 즉시 DLQ 처리
     */
    private DefaultErrorHandler createCommonErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            String dlqTopic,
            long initialIntervalMs,
            double multiplier,
            long maxIntervalMs,
            int maxRetries
    ) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxRetries);
        backOff.setInitialInterval(initialIntervalMs);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxIntervalMs);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(dlqTopic, record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }
}
