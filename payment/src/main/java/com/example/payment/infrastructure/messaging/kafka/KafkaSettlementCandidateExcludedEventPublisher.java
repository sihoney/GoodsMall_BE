package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.SettlementCandidateExcludedEvent;
import com.example.payment.domain.service.SettlementCandidateExcludedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.SettlementCandidateExcludedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaSettlementCandidateExcludedEventPublisher implements SettlementCandidateExcludedEventPublisher {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaSettlementCandidateExcludedEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${payment.kafka.topics.settlement-candidate-excluded:payment.settlement-candidate-excluded}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(SettlementCandidateExcludedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(new SettlementCandidateExcludedMessage(
                    event.eventId(),
                    event.refundId(),
                    event.orderId(),
                    event.escrowId(),
                    event.orderItemId(),
                    event.sellerMemberId(),
                    event.buyerMemberId(),
                    event.refundAmount(),
                    event.occurredAt().atZone(KOREA_ZONE_ID).toInstant()
            ));
            kafkaTemplate.send(topic, String.valueOf(event.escrowId()), message);
        } catch (Exception e) {
            log.error("Failed to serialize SettlementCandidateExcludedMessage. escrowId={}", event.escrowId(), e);
            throw new RuntimeException("Failed to serialize SettlementCandidateExcludedMessage", e);
        }
    }
}
