package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.domain.entity.OutboxEvent;
import com.example.settlement.domain.repository.OutboxRepository;
import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * settlement 지급 요청 이벤트를 outbox에 저장한다.
 */
@Slf4j
@Component
public class SettlementPayoutRequestedOutboxEventSaver {

    private static final String SELLER_SETTLEMENT_PAYOUT_REQUESTED_EVENT_TYPE = "SELLER_SETTLEMENT_PAYOUT_REQUESTED";
    private static final String SETTLEMENT_SOURCE = "settlement-service";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public SettlementPayoutRequestedOutboxEventSaver(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void save(SellerSettlementPayoutRequestedMessage message) {
        try {
            EventEnvelope<SellerSettlementPayoutRequestedMessage> envelope = new EventEnvelope<>(
                    message.eventId(),
                    SELLER_SETTLEMENT_PAYOUT_REQUESTED_EVENT_TYPE,
                    SETTLEMENT_SOURCE,
                    message.settlementId(),
                    message.sellerMemberId(),
                    message.requestedAt().atZone(KOREA_ZONE_ID).toInstant(),
                    resolveTraceId(message),
                    message
            );
            String payload = objectMapper.writeValueAsString(envelope);
            outboxRepository.save(OutboxEvent.create(
                    KafkaTopics.SETTLEMENT_PAYOUT_REQUESTED,
                    SELLER_SETTLEMENT_PAYOUT_REQUESTED_EVENT_TYPE,
                    String.valueOf(message.settlementId()),
                    envelope.traceId(),
                    payload
            ));
        } catch (Exception exception) {
            log.error("정산 지급 요청 outbox 저장 직렬화에 실패했습니다. settlementId={}", message.settlementId(), exception);
            throw new RuntimeException("정산 지급 요청 outbox 저장에 실패했습니다.", exception);
        }
    }

    private String resolveTraceId(SellerSettlementPayoutRequestedMessage message) {
        UUID eventId = message.eventId();
        if (eventId != null) {
            return eventId.toString();
        }
        return "settlement-seller-settlement-payout-requested";
    }
}
