package com.example.payment.infrastructure.config;

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

    /**
     * ProducerFactory Bean 등록
     * <p>
     * ProducerFactory는 Kafka Producer를 생성하는 공장 역할을 한다.
     * Kafka에 접속하기 위한 주소, key/value 직렬화 방식 같은
     * Producer의 기본 설정을 가지고 있다.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        // Kafka broker 주소 설정
        // Producer가 어느 Kafka 서버에 연결할지 지정한다.
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 메시지 key를 어떤 방식으로 바이트 배열로 변환할지 지정한다.
        // 현재 key 타입이 String 이므로 StringSerializer를 사용한다.
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 메시지 value를 어떤 방식으로 바이트 배열로 변환할지 지정한다.
        // 현재 value 타입도 String 이므로 StringSerializer를 사용한다.
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 위 설정값을 바탕으로 Kafka ProducerFactory를 생성한다.
        // 이 팩토리는 내부적으로 실제 Producer 인스턴스를 만들어준다.
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            // 위에서 등록한 ProducerFactory Bean을 주입받는다.
            ProducerFactory<String, String> producerFactory
    ) {
        // ProducerFactory를 기반으로 KafkaTemplate 생성
        return new KafkaTemplate<>(producerFactory);
    }
}
