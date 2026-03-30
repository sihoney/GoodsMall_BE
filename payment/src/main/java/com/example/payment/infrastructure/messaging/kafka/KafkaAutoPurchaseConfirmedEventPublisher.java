package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.domain.service.AutoPurchaseConfirmedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 자동 구매확정 내부 이벤트를 Kafka 계약 메시지로 변환해 발행하는 adapter다.
 */
public class KafkaAutoPurchaseConfirmedEventPublisher implements AutoPurchaseConfirmedEventPublisher {

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
    /**
     * 내부 이벤트를 Kafka 메시지로 변환하고 orderId를 key로 발행한다.
     */
    public void publish(AutoPurchaseConfirmedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(new AutoPurchaseConfirmedMessage(
                    event.orderId(),
                    event.buyerMemberId(),
                    event.confirmedAt()
            ));
            kafkaTemplate.send(topic, String.valueOf(event.orderId()), message);
        } catch (Exception e) {
            log.error("Failed to serialize AutoPurchaseConfirmedMessage. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to serialize AutoPurchaseConfirmedMessage", e);
        }
    }
}
