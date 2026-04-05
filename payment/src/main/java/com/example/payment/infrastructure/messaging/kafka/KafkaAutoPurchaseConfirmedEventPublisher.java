package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.domain.service.AutoPurchaseConfirmedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 자동 구매확정 완료 이벤트를 Kafka 계약 메시지로 변환해 발행하는 adapter다.
 */
public class KafkaAutoPurchaseConfirmedEventPublisher implements AutoPurchaseConfirmedEventPublisher {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaAutoPurchaseConfirmedEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${payment.kafka.topics.auto-purchase-confirmed:payment.auto-purchase-confirmed}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(AutoPurchaseConfirmedEvent event) {
        try {
            // 객체를 Json 문자열로 직렬화
            // ObjectMapper는 Jackson 라이브러리의 JSON 변환 도구
            String message = objectMapper.writeValueAsString(new AutoPurchaseConfirmedMessage(
                    event.orderId(),
                    event.buyerMemberId(),
                    event.confirmedAt().atZone(KOREA_ZONE_ID).toInstant()
            ));
            // 카프카 전송 메서드(어디로 보낼지, 메시지 키->문자열로 변환, 메시지 본문)
            // todo : 비동기 전송이므로 필요시 발행 성공 보장 로직 추가
            kafkaTemplate.send(topic, String.valueOf(event.orderId()), message);
        } catch (Exception e) {
            log.error("Failed to serialize AutoPurchaseConfirmedMessage. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to serialize AutoPurchaseConfirmedMessage", e);
        }
    }
}
