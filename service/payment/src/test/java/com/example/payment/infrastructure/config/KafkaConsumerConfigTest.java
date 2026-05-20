package com.example.payment.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@DisplayName("KafkaConsumerConfig 테스트")
class KafkaConsumerConfigTest {

    private final KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig();

    @Test
    @DisplayName("member created consumer는 StringDeserializer를 사용한다")
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
