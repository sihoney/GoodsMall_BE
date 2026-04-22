package com.example.notification.infrastructure.messaging.kafka.handler;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import com.example.notification.infrastructure.messaging.kafka.dlq.InvalidEventPayloadException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerSettlementPayoutResultNotificationEventHandler implements NotificationEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE = "SELLER_SETTLEMENT_PAYOUT_RESULT";

    private final NotificationUsecase notificationUsecase;
    private final ObjectMapper objectMapper;

    @Override
    public String supportsEventType() {
        return SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE;
    }

    @Override
    public void handle(EventEnvelope<JsonNode> event) {
        EventEnvelope<SellerSettlementPayoutResultMessage> typedEvent = new EventEnvelope<>(
                event.eventId(),
                event.eventType(),
                event.source(),
                event.aggregateId(),
                event.recipientId(),
                event.occurredAt(),
                event.traceId(),
                objectMapper.convertValue(event.payload(), SellerSettlementPayoutResultMessage.class)
        );

        validateSellerSettlementPayoutResultEvent(typedEvent);

        LocalDateTime processedAt = toKoreaLocalDateTime(typedEvent.occurredAt());
        SellerSettlementPayoutResultMessage payload = typedEvent.payload();
        if (payload.resultStatus() == SellerSettlementPayoutResultStatus.SUCCESS) {
            notificationUsecase.createSellerSettlementPayoutSucceededNotification(
                    typedEvent.eventId(),
                    typedEvent.traceId(),
                    payload.settlementId(),
                    payload.sellerMemberId(),
                    payload.payoutAmount(),
                    processedAt
            );
            return;
        }

        notificationUsecase.createSellerSettlementPayoutFailedNotification(
                typedEvent.eventId(),
                typedEvent.traceId(),
                payload.settlementId(),
                payload.sellerMemberId(),
                payload.failureReason(),
                processedAt
        );
    }

    private void validateSellerSettlementPayoutResultEvent(EventEnvelope<SellerSettlementPayoutResultMessage> event) {
        if (event == null) {
            throw new InvalidEventPayloadException("sellerSettlementPayoutResult event is required.");
        }
        if (!SELLER_SETTLEMENT_PAYOUT_RESULT_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidEventPayloadException("Unsupported eventType: " + event.eventType());
        }
        if (event.eventId() == null) {
            throw new InvalidEventPayloadException("eventId is required.");
        }
        if (event.source() == null || event.source().isBlank()) {
            throw new InvalidEventPayloadException("source is required.");
        }
        if (event.recipientId() == null) {
            throw new InvalidEventPayloadException("recipientId is required.");
        }
        if (event.occurredAt() == null) {
            throw new InvalidEventPayloadException("occurredAt is required.");
        }
        if (event.traceId() == null || event.traceId().isBlank()) {
            throw new InvalidEventPayloadException("traceId is required.");
        }
        if (event.payload() == null) {
            throw new InvalidEventPayloadException("payload is required.");
        }
        if (event.payload().settlementId() == null) {
            throw new InvalidEventPayloadException("payload.settlementId is required.");
        }
        if (event.payload().sellerMemberId() == null) {
            throw new InvalidEventPayloadException("payload.sellerMemberId is required.");
        }
        if (event.payload().resultStatus() == null) {
            throw new InvalidEventPayloadException("payload.resultStatus is required.");
        }
        if (event.payload().resultStatus() == SellerSettlementPayoutResultStatus.SUCCESS
                && (event.payload().payoutAmount() == null || event.payload().payoutAmount() <= 0)) {
            throw new InvalidEventPayloadException("payload.payoutAmount must be positive for successful payout.");
        }
        if (event.payload().resultStatus() == SellerSettlementPayoutResultStatus.FAILED
                && event.payload().failureReason() == null) {
            throw new InvalidEventPayloadException("payload.failureReason is required for failed payout.");
        }
        if (!Objects.equals(event.recipientId(), event.payload().sellerMemberId())) {
            throw new InvalidEventPayloadException("recipientId and payload.sellerMemberId must match.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
