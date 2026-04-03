package com.example.order.config;

import com.example.order.infrastructure.kafka.event.PaymentResultEvent;
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

    @Bean
    public ConsumerFactory<String, PaymentResultEvent> paymentResultConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JacksonJsonDeserializer<PaymentResultEvent> valueDeserializer =
                new JacksonJsonDeserializer<>(PaymentResultEvent.class);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    /**
     * {@code @KafkaListener} 기본 containerFactory 이름과 일치시킨다.
     */
    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PaymentResultEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentResultEvent> paymentResultConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentResultConsumerFactory);
        return factory;
    }
}
