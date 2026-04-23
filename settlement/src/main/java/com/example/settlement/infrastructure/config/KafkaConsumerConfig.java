package com.example.settlement.infrastructure.config;

import com.example.settlement.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.settlement.infrastructure.messaging.kafka.KafkaRetryPolicy;
import com.example.settlement.infrastructure.messaging.kafka.KafkaTopics;
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

/**
 * settlement 모듈 Kafka consumer(소비기) 설정을 담당한다.
 * <p>
 * 정산 원천/지급 결과 소비 경로에 retry(재시도)/backoff(백오프)/DLQ(사후처리큐) 정책을 적용한다.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> settlementCandidateCreatedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConsumerGroups.SETTLEMENT_SERVICE);
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
    public ConcurrentKafkaListenerContainerFactory<String, String>
        settlementCandidateCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> settlementCandidateCreatedConsumerFactory,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(settlementCandidateCreatedConsumerFactory);
        factory.setCommonErrorHandler(createCommonErrorHandler(
                kafkaTemplate,
                KafkaTopics.SETTLEMENT_CANDIDATE_CREATED_DLQ
        ));
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> sellerSettlementPayoutResultConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConsumerGroups.SETTLEMENT_SERVICE);
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
    public ConcurrentKafkaListenerContainerFactory<String, String>
        sellerSettlementPayoutResultKafkaListenerContainerFactory(
            ConsumerFactory<String, String> sellerSettlementPayoutResultConsumerFactory,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sellerSettlementPayoutResultConsumerFactory);
        factory.setCommonErrorHandler(createCommonErrorHandler(
                kafkaTemplate,
                KafkaTopics.SETTLEMENT_PAYOUT_RESULT_DLQ
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
            KafkaTemplate<String, String> kafkaTemplate,
            String dlqTopic
    ) {
        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(KafkaRetryPolicy.MAX_RETRIES);
        backOff.setInitialInterval(KafkaRetryPolicy.INITIAL_INTERVAL_MS);
        backOff.setMultiplier(KafkaRetryPolicy.MULTIPLIER);
        backOff.setMaxInterval(KafkaRetryPolicy.MAX_INTERVAL_MS);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(dlqTopic, record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }
}
