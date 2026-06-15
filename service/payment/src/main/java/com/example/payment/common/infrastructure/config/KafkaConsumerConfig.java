package com.example.payment.common.infrastructure.config;

import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaRetryPolicy;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.common.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, MemberCreatedMessage> memberCreatedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, MemberCreatedMessage.class);
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
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, OrderPurchaseConfirmedMessage.class);
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
    public ConsumerFactory<String, String> auctionBidFeeChargeRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, String.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        auctionBidFeeChargeRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> auctionBidFeeChargeRequestedConsumerFactory,
            @Value("${kafka.consumer.auction-bid-fee.concurrency:8}") int concurrency
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(auctionBidFeeChargeRequestedConsumerFactory);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(createAuctionBidFeeChargeRequestedErrorHandler());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> auctionBidFeeRefundRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, String.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        auctionBidFeeRefundRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> auctionBidFeeRefundRequestedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(auctionBidFeeRefundRequestedConsumerFactory);
        factory.setCommonErrorHandler(createAuctionBidFeeChargeRequestedErrorHandler());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> sellerSettlementPayoutRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, String.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        sellerSettlementPayoutRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> sellerSettlementPayoutRequestedConsumerFactory,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sellerSettlementPayoutRequestedConsumerFactory);
        factory.setCommonErrorHandler(createPayoutRequestedErrorHandler(kafkaTemplate));
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

    private DefaultErrorHandler createAuctionBidFeeChargeRequestedErrorHandler() {
        return new DefaultErrorHandler((record, exception) ->
                log.error("Auction bid fee charge request Kafka handling failed: topic={}, partition={}, offset={}, key={}, payloadSnippet={}",
                        record.topic(), record.partition(), record.offset(), record.key(), summarizePayload(record), exception),
                new FixedBackOff(0L, 0L));
    }

    private DefaultErrorHandler createPayoutRequestedErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(KafkaRetryPolicy.MAX_RETRIES);
        backOff.setInitialInterval(KafkaRetryPolicy.INITIAL_INTERVAL_MS);
        backOff.setMultiplier(KafkaRetryPolicy.MULTIPLIER);
        backOff.setMaxInterval(KafkaRetryPolicy.MAX_INTERVAL_MS);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(KafkaTopics.SETTLEMENT_PAYOUT_REQUESTED_DLQ, record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, WalletNotFoundException.class);

        return errorHandler;
    }

    private String summarizePayload(ConsumerRecord<?, ?> record) {
        Object value = record.value();
        if (value == null) {
            return "<empty>";
        }
        String normalized = value.toString().replaceAll("\\s+", " ").trim();
        return normalized.length() <= 300 ? normalized : normalized.substring(0, 300) + "...";
    }
}
