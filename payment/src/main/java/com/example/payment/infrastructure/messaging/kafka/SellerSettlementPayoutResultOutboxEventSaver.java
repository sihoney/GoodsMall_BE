package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.OutboxEventPendingTrigger;
import com.example.payment.domain.entity.OutboxEvent;
import com.example.payment.domain.repository.OutboxRepository;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * settlement 지급 결과 이벤트를 payment outbox에 저장한다.
 */
@Slf4j
@Component
public class SellerSettlementPayoutResultOutboxEventSaver {

    private static final String SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE = "SELLER_SETTLEMENT_PAYOUT_RESULT";
    private static final String PAYMENT_SOURCE = "payment-service";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SellerSettlementPayoutResultOutboxEventSaver(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void save(SellerSettlementPayoutResultMessage event) {
        try {
            EventEnvelope<SellerSettlementPayoutResultMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.settlementId(),
                    event.sellerMemberId(),
                    event.processedAt().atZone(KOREA_ZONE_ID).toInstant(),
                    resolveTraceId(event),
                    event
            );
            String message = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    KafkaTopics.SETTLEMENT_PAYOUT_RESULT,
                    SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE,
                    String.valueOf(event.settlementId()),
                    resolveTraceId(event),
                    message
            );
            outboxRepository.save(outboxEvent);
            applicationEventPublisher.publishEvent(new OutboxEventPendingTrigger());
        } catch (Exception e) {
            log.error("Failed to serialize SellerSettlementPayoutResultMessage. settlementId={}", event.settlementId(), e);
            throw new RuntimeException("Failed to serialize SellerSettlementPayoutResultMessage", e);
        }
    }

    private String resolveTraceId(SellerSettlementPayoutResultMessage event) {
        UUID requestEventId = event.requestEventId();
        if (requestEventId != null) {
            return requestEventId.toString();
        }
        UUID eventId = event.eventId();
        if (eventId != null) {
            return eventId.toString();
        }
        return "payment-seller-settlement-payout-result";
    }
}
