package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.application.usecase.MonthlySettlementUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * payment -> settlement 정산 후보 알림 이벤트를 소비해 settlement item으로 적재한다.
 */
@Slf4j
@Component
public class SettlementCandidateCreatedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE = "SETTLEMENT_CANDIDATE_CREATED";
    private static final TypeReference<EventEnvelope<SettlementCandidateCreatedMessage>>
            SETTLEMENT_CANDIDATE_CREATED_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final MonthlySettlementUseCase monthlySettlementService;
    private final ObjectMapper objectMapper;

    public SettlementCandidateCreatedEventConsumer(MonthlySettlementUseCase monthlySettlementService, ObjectMapper objectMapper) {
        this.monthlySettlementService = monthlySettlementService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.SETTLEMENT_CANDIDATE_CREATED,
            groupId = KafkaConsumerGroups.SETTLEMENT_SERVICE,
            containerFactory = "settlementCandidateCreatedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            EventEnvelope<SettlementCandidateCreatedMessage> envelope = readEnvelope(eventJson);
            validateEnvelope(envelope);
            SettlementCandidateCreatedMessage event = envelope.payload();
            monthlySettlementService.registerSettlementItem(new SettlementItemCreateCommand(
                    event.orderId(),
                    event.escrowId(),
                    event.sellerMemberId(),
                    event.grossAmount(),
                    toKoreaLocalDateTime(event.releasedAt())
            ));
        } catch (Exception e) {
            log.error("Failed to process settlement candidate created envelope", e);
            throw new RuntimeException("Failed to deserialize settlement candidate created envelope", e);
        }
    }

    private EventEnvelope<SettlementCandidateCreatedMessage> readEnvelope(String eventJson) throws Exception {
        return objectMapper.readValue(eventJson, SETTLEMENT_CANDIDATE_CREATED_ENVELOPE_TYPE);
    }

    private void validateEnvelope(EventEnvelope<SettlementCandidateCreatedMessage> envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("settlementCandidateCreated envelope is required.");
        }
        if (envelope.eventId() == null) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (envelope.eventType() == null || envelope.eventType().isBlank()) {
            throw new IllegalArgumentException("eventType is required.");
        }
        if (!SETTLEMENT_CANDIDATE_CREATED_EVENT_TYPE.equals(envelope.eventType())) {
            throw new IllegalArgumentException("eventType is invalid.");
        }
        if (envelope.source() == null || envelope.source().isBlank()) {
            throw new IllegalArgumentException("source is required.");
        }
        if (envelope.aggregateId() == null) {
            throw new IllegalArgumentException("aggregateId is required.");
        }
        if (envelope.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required.");
        }
        if (envelope.traceId() == null || envelope.traceId().isBlank()) {
            throw new IllegalArgumentException("traceId is required.");
        }

        SettlementCandidateCreatedMessage event = envelope.payload();
        if (event == null) {
            throw new IllegalArgumentException("payload is required.");
        }
        if (event.orderId() == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (event.escrowId() == null) {
            throw new IllegalArgumentException("escrowId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new IllegalArgumentException("sellerMemberId is required.");
        }
        if (event.grossAmount() == null || event.grossAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("grossAmount must be positive.");
        }
        if (event.releasedAt() == null) {
            throw new IllegalArgumentException("releasedAt is required.");
        }
        if (!envelope.aggregateId().equals(event.escrowId())) {
            throw new IllegalArgumentException("aggregateId must match escrowId.");
        }
        if (envelope.recipientId() != null && !envelope.recipientId().equals(event.sellerMemberId())) {
            throw new IllegalArgumentException("recipientId must match sellerMemberId.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
