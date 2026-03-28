package com.example.settlement.infrastructure.config;

import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
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
    public ConsumerFactory<String, SettlementCandidateCreatedMessage> settlementCandidateCreatedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${settlement.kafka.consumer-groups.settlement-candidate-created:settlement-service}") String groupId
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put("spring.json.trusted.packages", "com.example.settlement.infrastructure.messaging.kafka.contract");
        props.put("spring.json.use.type.headers", false);
        props.put("spring.json.value.default.type", SettlementCandidateCreatedMessage.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SettlementCandidateCreatedMessage>
        settlementCandidateCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, SettlementCandidateCreatedMessage> settlementCandidateCreatedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, SettlementCandidateCreatedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(settlementCandidateCreatedConsumerFactory);
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
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put("spring.json.trusted.packages", "com.example.settlement.infrastructure.messaging.kafka.contract");
        props.put("spring.json.use.type.headers", false);
        props.put("spring.json.value.default.type", SellerSettlementPayoutResultMessage.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SellerSettlementPayoutResultMessage>
        sellerSettlementPayoutResultKafkaListenerContainerFactory(
            ConsumerFactory<String, SellerSettlementPayoutResultMessage> sellerSettlementPayoutResultConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, SellerSettlementPayoutResultMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sellerSettlementPayoutResultConsumerFactory);
        return factory;
    }
}
