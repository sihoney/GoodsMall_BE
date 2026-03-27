package com.example.payment.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@DisplayName("KafkaConsumerConfig 테스트")
class KafkaConsumerConfigTest {

    private final KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig();

    @Test
    @DisplayName("member created consumer는 JacksonJsonDeserializer를 사용한다")
    void memberCreatedConsumerFactory_usesJacksonJsonDeserializer() {
        ConsumerFactory<String, MemberCreatedMessage> consumerFactory = kafkaConsumerConfig.memberCreatedConsumerFactory(
                "localhost:9092",
                "payment-service"
        );

        assertThat(consumerFactory).isInstanceOf(DefaultKafkaConsumerFactory.class);
        DefaultKafkaConsumerFactory<?, ?> defaultFactory = (DefaultKafkaConsumerFactory<?, ?>) consumerFactory;
        assertThat(defaultFactory.getConfigurationProperties().get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(JacksonJsonDeserializer.class);
    }
}
