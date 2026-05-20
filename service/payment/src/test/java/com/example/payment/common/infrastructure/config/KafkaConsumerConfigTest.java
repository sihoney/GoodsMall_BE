package com.example.payment.common.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@DisplayName("payment test")
class KafkaConsumerConfigTest {

    private final KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig();

    @Test
    @DisplayName("payment test")
    void memberCreatedConsumerFactory_usesStringDeserializer() {
        ConsumerFactory<String, MemberCreatedMessage> consumerFactory = kafkaConsumerConfig.memberCreatedConsumerFactory(
                "localhost:9092"
        );

        assertThat(consumerFactory).isInstanceOf(DefaultKafkaConsumerFactory.class);
        DefaultKafkaConsumerFactory<?, ?> defaultFactory = (DefaultKafkaConsumerFactory<?, ?>) consumerFactory;
        assertThat(defaultFactory.getConfigurationProperties().get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(StringDeserializer.class);
        assertThat(defaultFactory.getConfigurationProperties().get(ConsumerConfig.GROUP_ID_CONFIG))
                .isEqualTo(KafkaConsumerGroups.PAYMENT_SERVICE);
    }
}
