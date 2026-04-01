package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.domain.service.SettlementCandidateCreatedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaSettlementCandidateCreatedEventPublisher implements SettlementCandidateCreatedEventPublisher {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaSettlementCandidateCreatedEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${payment.kafka.topics.settlement-candidate-created:payment.settlement-candidate-created}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(SettlementCandidateCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(new SettlementCandidateCreatedMessage(
                    event.eventId(),
                    event.orderId(),
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.grossAmount(),
                    event.releasedAt().atZone(KOREA_ZONE_ID).toInstant(),
                    event.confirmationType(),
                    event.occurredAt().atZone(KOREA_ZONE_ID).toInstant()
            ));
            kafkaTemplate.send(topic, String.valueOf(event.escrowId()), message);
        } catch (Exception e) {
            log.error("Failed to serialize SettlementCandidateCreatedMessage. escrowId={}", event.escrowId(), e);
            throw new RuntimeException("Failed to serialize SettlementCandidateCreatedMessage", e);
        }
    }
}
