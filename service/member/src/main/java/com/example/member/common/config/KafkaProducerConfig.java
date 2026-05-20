package com.example.member.common.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers; // Kafka 브로커 주소

    // KafkaProducer를 위한 ProducerFactory 빈 정의
    @Bean
    public ProducerFactory<String, String> memberSignedUpEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        // ProducerConfig 설정: Kafka 클라이언트 라이브러리에서 제공하는 상수 클래스, 키를 문자열로 직접 쓰지 않게 해주는 상수 모음
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // KafkaTemplate 빈 정의 : KafkaProducer 역할을 하는 컴포넌트에서 주입받아 사용
    @Bean
    public KafkaTemplate<String, String> memberSignedUpEventKafkaTemplate(
            ProducerFactory<String, String> memberSignedUpEventProducerFactory
    ) {
        return new KafkaTemplate<>(memberSignedUpEventProducerFactory);
    }
}
