package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.domain.service.SettlementCandidateCreatedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaSettlementCandidateCreatedEventPublisher implements SettlementCandidateCreatedEventPublisher {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE = "SETTLEMENT_CANDIDATE_CREATED";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaSettlementCandidateCreatedEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(SettlementCandidateCreatedEvent event) {
        try {
            SettlementCandidateCreatedMessage payload = new SettlementCandidateCreatedMessage(
                    event.eventId(),
                    event.orderId(),
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.grossAmount(),
                    event.releasedAt().atZone(KOREA_ZONE_ID).toInstant(),
                    event.confirmationType(),
                    event.occurredAt().atZone(KOREA_ZONE_ID).toInstant()
            );
            EventEnvelope<SettlementCandidateCreatedMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.occurredAt().atZone(KOREA_ZONE_ID).toInstant(),
                    resolveTraceId(event),
                    payload
            );
            String message = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.SETTLEMENT_CANDIDATE_CREATED, String.valueOf(event.escrowId()), message);
        } catch (Exception e) {
            log.error("Failed to serialize SettlementCandidateCreatedMessage. escrowId={}", event.escrowId(), e);
            throw new RuntimeException("Failed to serialize SettlementCandidateCreatedMessage", e);
        }
    }

    private String resolveTraceId(SettlementCandidateCreatedEvent event) {
        UUID eventId = event.eventId();
        if (eventId == null) {
            return "payment-settlement-candidate-created";
        }
        return eventId.toString();
    }
}
