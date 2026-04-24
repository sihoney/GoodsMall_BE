package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.application.usecase.SettlementPayoutUseCase;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * payment -> settlement 정산 지급 결과 이벤트를 소비하는 Kafka consumer(소비기)다.
 */
@Slf4j
@Component
public class SellerSettlementPayoutResultEventConsumer {

    private static final String SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE = "SELLER_SETTLEMENT_PAYOUT_RESULT";
    private static final TypeReference<EventEnvelope<SellerSettlementPayoutResultMessage>>
            SELLER_SETTLEMENT_PAYOUT_RESULT_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final SettlementPayoutUseCase settlementPayoutService;
    private final ObjectMapper objectMapper;

    public SellerSettlementPayoutResultEventConsumer(SettlementPayoutUseCase settlementPayoutService, ObjectMapper objectMapper) {
        this.settlementPayoutService = settlementPayoutService;
        this.objectMapper = objectMapper;
    }

    /**
     * 지급 결과 이벤트를 settlement 상태 반영 서비스로 전달한다.
     * transport 계층에서는 비즈니스 분기 없이 이벤트를 그대로 전달하고,
     * 상태 전이 정책은 application service가 전담한다.
     */
    @KafkaListener(
            topics = KafkaTopics.SETTLEMENT_PAYOUT_RESULT,
            groupId = KafkaConsumerGroups.SETTLEMENT_SERVICE,
            containerFactory = "sellerSettlementPayoutResultKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            EventEnvelope<SellerSettlementPayoutResultMessage> envelope = readEnvelope(eventJson);
            validateEnvelope(envelope);
            SellerSettlementPayoutResultMessage event = envelope.payload();
            settlementPayoutService.applyPayoutResult(event);
        } catch (Exception e) {
            log.error("Failed to process seller settlement payout result envelope", e);
            throw new RuntimeException("Failed to deserialize seller settlement payout result envelope", e);
        }
    }

    private EventEnvelope<SellerSettlementPayoutResultMessage> readEnvelope(String eventJson) throws Exception {
        return objectMapper.readValue(eventJson, SELLER_SETTLEMENT_PAYOUT_RESULT_ENVELOPE_TYPE);
    }

    private void validateEnvelope(EventEnvelope<SellerSettlementPayoutResultMessage> envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("sellerSettlementPayoutResult envelope is required.");
        }
        if (envelope.eventId() == null) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (envelope.eventType() == null || envelope.eventType().isBlank()) {
            throw new IllegalArgumentException("eventType is required.");
        }
        if (!SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE.equals(envelope.eventType())) {
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

        SellerSettlementPayoutResultMessage event = envelope.payload();
        if (event == null) {
            throw new IllegalArgumentException("payload is required.");
        }
        if (event.settlementId() == null) {
            throw new IllegalArgumentException("settlementId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new IllegalArgumentException("sellerMemberId is required.");
        }
        if (!envelope.aggregateId().equals(event.settlementId())) {
            throw new IllegalArgumentException("aggregateId must match settlementId.");
        }
        if (envelope.recipientId() != null && !envelope.recipientId().equals(event.sellerMemberId())) {
            throw new IllegalArgumentException("recipientId must match sellerMemberId.");
        }
    }
}

