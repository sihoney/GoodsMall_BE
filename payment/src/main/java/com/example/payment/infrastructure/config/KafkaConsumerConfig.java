package com.example.payment.infrastructure.config;

import com.example.payment.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderDeliveryCompletedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

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
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class
        );
        props.put("spring.json.trusted.packages", "com.example.payment.infrastructure.messaging.kafka.contract");
        props.put("spring.json.use.type.headers", false);
        props.put("spring.json.value.default.type", targetType.getName());

        return new DefaultKafkaConsumerFactory<>(props);
    }
}
