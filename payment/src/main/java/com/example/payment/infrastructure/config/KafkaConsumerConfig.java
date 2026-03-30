package com.example.payment.infrastructure.config;

import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderDeliveryCompletedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
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
 * payment 모듈 Kafka consumer(소비기) 설정을 담당한다.
 * <p>
 * 정산 지급 요청 소비 경로에는 retry(재시도)/backoff(백오프)/DLQ(사후처리큐) 정책을 연결한다.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, MemberCreatedMessage> memberCreatedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${payment.kafka.consumer-groups.member-created:payment-service}") String groupId
    ) {
        return createConsumerFactory(bootstrapServers, groupId, MemberCreatedMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MemberCreatedMessage>
        memberCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, MemberCreatedMessage> memberCreatedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, MemberCreatedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(memberCreatedConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderPurchaseConfirmedMessage> orderPurchaseConfirmedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${payment.kafka.consumer-groups.order-purchase-confirmed:payment-service}") String groupId
    ) {
        return createConsumerFactory(bootstrapServers, groupId, OrderPurchaseConfirmedMessage.class);
    }

    @Bean
    public ConsumerFactory<String, OrderPaymentRequestedMessage> orderPaymentRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${payment.kafka.consumer-groups.order-payment-requested:payment-service}") String groupId
    ) {
        return createConsumerFactory(bootstrapServers, groupId, OrderPaymentRequestedMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentRequestedMessage>
        orderPaymentRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderPaymentRequestedMessage> orderPaymentRequestedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPaymentRequestedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPaymentRequestedConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderDeliveryCompletedMessage> orderDeliveryCompletedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${payment.kafka.consumer-groups.order-delivery-completed:payment-service}") String groupId
    ) {
        return createConsumerFactory(bootstrapServers, groupId, OrderDeliveryCompletedMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderDeliveryCompletedMessage>
        orderDeliveryCompletedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderDeliveryCompletedMessage> orderDeliveryCompletedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderDeliveryCompletedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderDeliveryCompletedConsumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPurchaseConfirmedMessage>
        orderPurchaseConfirmedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderPurchaseConfirmedMessage> orderPurchaseConfirmedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPurchaseConfirmedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPurchaseConfirmedConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, SellerSettlementPayoutRequestedMessage> sellerSettlementPayoutRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${payment.kafka.consumer-groups.settlement-payout-requested:payment-service}") String groupId
    ) {
        return createConsumerFactory(bootstrapServers, groupId, SellerSettlementPayoutRequestedMessage.class);
    }

    /**
     * settlement 지급 요청 소비 전용 리스너 팩토리를 생성한다.
     * <p>
     * 처리 실패 시 공통 에러 처리기로 백오프 재시도 후 DLQ 발행을 수행한다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SellerSettlementPayoutRequestedMessage>
        sellerSettlementPayoutRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, SellerSettlementPayoutRequestedMessage> sellerSettlementPayoutRequestedConsumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${payment.kafka.retry.settlement-payout-requested.dlq-topic:settlement.seller-payout-requested.dlq}") String dlqTopic,
            @Value("${payment.kafka.retry.settlement-payout-requested.initial-interval-ms:1000}") long initialIntervalMs,
            @Value("${payment.kafka.retry.settlement-payout-requested.multiplier:2.0}") double multiplier,
            @Value("${payment.kafka.retry.settlement-payout-requested.max-interval-ms:10000}") long maxIntervalMs,
            @Value("${payment.kafka.retry.settlement-payout-requested.max-retries:3}") int maxRetries
    ) {
        ConcurrentKafkaListenerContainerFactory<String, SellerSettlementPayoutRequestedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sellerSettlementPayoutRequestedConsumerFactory);
        factory.setCommonErrorHandler(createPayoutRequestedErrorHandler(
                kafkaTemplate,
                dlqTopic,
                initialIntervalMs,
                multiplier,
                maxIntervalMs,
                maxRetries
        ));
        return factory;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(
            String bootstrapServers,
            String groupId,
            Class<T> targetType
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
     * 정산 지급 요청 소비 실패에 대한 공통 에러 처리기를 생성한다.
     * <p>
     * - RETRYABLE 예외: 지수 백오프 재시도
     * - 재시도 소진: DLQ 토픽으로 발행
     * - IllegalArgumentException, WalletNotFoundException: 비재시도 예외로 즉시 DLQ 처리
     */
    private DefaultErrorHandler createPayoutRequestedErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
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
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, WalletNotFoundException.class);
        return errorHandler;
    }
}
