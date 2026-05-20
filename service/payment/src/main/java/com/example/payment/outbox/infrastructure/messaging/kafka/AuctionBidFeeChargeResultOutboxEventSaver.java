package com.example.payment.outbox.infrastructure.messaging.kafka;


import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.outbox.application.event.OutboxEventPendingTrigger;
import com.example.payment.outbox.domain.entity.OutboxEvent;
import com.example.payment.outbox.domain.repository.OutboxRepository;
import com.example.payment.common.infrastructure.messaging.kafka.contract.BidFeeChargeFailedMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.BidFeeChargeSucceededMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ 寃곌낵 ?대깽?몃? payment outbox????ν븳??
 */
@Slf4j
@Component
public class AuctionBidFeeChargeResultOutboxEventSaver {

    private static final String PAYMENT_SOURCE = "payment-service";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AuctionBidFeeChargeResultOutboxEventSaver(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void saveSuccess(String eventType, BidFeeChargeSucceededMessage event) {
        EventEnvelope<BidFeeChargeSucceededMessage> envelope = new EventEnvelope<>(
                event.eventId(),
                eventType,
                PAYMENT_SOURCE,
                event.auctionId(),
                null,
                event.occurredAt(),
                resolveTraceId(event.eventId(), event.bidId(), "payment-bid-fee-charge-succeeded"),
                event
        );
        save(KafkaTopics.AUCTION_BID_FEE_CHARGE_SUCCEEDED, eventType, String.valueOf(event.bidId()), envelope);
    }

    @Transactional
    public void saveFailure(String eventType, BidFeeChargeFailedMessage event) {
        EventEnvelope<BidFeeChargeFailedMessage> envelope = new EventEnvelope<>(
                event.eventId(),
                eventType,
                PAYMENT_SOURCE,
                event.auctionId(),
                null,
                event.occurredAt(),
                resolveTraceId(event.eventId(), event.bidId(), "payment-bid-fee-charge-failed"),
                event
        );
        save(KafkaTopics.AUCTION_BID_FEE_CHARGE_FAILED, eventType, String.valueOf(event.bidId()), envelope);
    }

    private void save(String topic, String eventType, String aggregateId, EventEnvelope<?> envelope) {
        try {
            String message = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    topic,
                    eventType,
                    aggregateId,
                    envelope.traceId(),
                    message
            );
            outboxRepository.save(outboxEvent);
            applicationEventPublisher.publishEvent(new OutboxEventPendingTrigger());
        } catch (Exception e) {
            log.error("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ 寃곌낵 ?대깽??吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎. topic={} aggregateId={}", topic, aggregateId, e);
            throw new RuntimeException("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ 寃곌낵 ?대깽??吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    private String resolveTraceId(UUID eventId, UUID bidId, String fallback) {
        if (bidId != null) {
            return bidId.toString();
        }
        if (eventId != null) {
            return eventId.toString();
        }
        return fallback;
    }
}
