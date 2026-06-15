package com.example.payment.outbox.infrastructure.messaging.kafka;


import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.outbox.application.event.OutboxEventPendingTrigger;
import com.example.payment.outbox.domain.entity.OutboxEvent;
import com.example.payment.outbox.domain.repository.OutboxRepository;
import com.example.payment.common.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * settlement 吏湲?寃곌낵 ?대깽?몃? payment outbox????ν븳??
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
            log.error("SellerSettlementPayoutResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎. settlementId={}", event.settlementId(), e);
            throw new RuntimeException("SellerSettlementPayoutResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", e);
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
