package com.example.notification.infrastructure.config;

import com.example.notification.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
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
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    // AutoPurchaseConfirmedMessage 관련 Bean 설정
    @Bean
    public ConsumerFactory<String, AutoPurchaseConfirmedMessage> autoPurchaseConfirmedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${notification.kafka.consumer-groups.auto-purchase-confirmed:notification-service}") String groupId
    ) {
        return createConsumerFactory(bootstrapServers, groupId, AutoPurchaseConfirmedMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AutoPurchaseConfirmedMessage> autoPurchaseConfirmedKafkaListenerContainerFactory(
            ConsumerFactory<String, AutoPurchaseConfirmedMessage> autoPurchaseConfirmedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, AutoPurchaseConfirmedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(autoPurchaseConfirmedConsumerFactory);
        return factory;
    }

    // OrderPaymentResultMessage 관련 Bean 설정
    @Bean
    public ConsumerFactory<String, OrderPaymentResultMessage> orderPaymentResultConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${notification.kafka.consumer-groups.order-payment-result:notification-service}") String groupId
    ) {
        return createConsumerFactory(bootstrapServers, groupId, OrderPaymentResultMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentResultMessage>
        orderPaymentResultKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderPaymentResultMessage> orderPaymentResultConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPaymentResultMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPaymentResultConsumerFactory);
        return factory;
    }

    // 공통 ConsumerFactory 생성 메서드
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
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put("spring.json.trusted.packages", "com.example.notification.infrastructure.messaging.kafka.contract");
        props.put("spring.json.use.type.headers", false);
        props.put("spring.json.value.default.type", targetType.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
