package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 수동 구매확정 이벤트를 escrow release 유스케이스로 연결하는 Kafka consumer다.
 * consumer는 MANUAL 계약 검증과 command 변환만 담당하고, 정산 정책은 usecase에 위임한다.
 */
public class OrderPurchaseConfirmedEventConsumer {

    private static final String ORDER_PURCHASE_CONFIRMED_EVENT_TYPE = "ORDER_PURCHASE_CONFIRMED";
    private static final TypeReference<EventEnvelope<OrderPurchaseConfirmedMessage>> ORDER_PURCHASE_CONFIRMED_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final EscrowReleaseUseCase escrowReleaseUseCase;
    private final ObjectMapper objectMapper;

    public OrderPurchaseConfirmedEventConsumer(EscrowReleaseUseCase escrowReleaseUseCase, ObjectMapper objectMapper) {
        this.escrowReleaseUseCase = escrowReleaseUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_PURCHASE_CONFIRMED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "orderPurchaseConfirmedKafkaListenerContainerFactory"
    )
    /**
     * 수동 구매확정 이벤트만 escrow release 요청으로 전달한다.
     * AUTO 구매확정은 scheduler 경로에서 처리되므로 consumer 단계에서 차단한다.
     */
    // todo: 수동 구매 확정 또한 사용자가 요청하는 반응이므로 필요시 order와 api 통신을 써야할지 고려
    public void listen(String eventJson) {
        try {
            EventEnvelope<OrderPurchaseConfirmedMessage> event = objectMapper.readValue(
                    eventJson,
                    ORDER_PURCHASE_CONFIRMED_ENVELOPE_TYPE
            );
            validateEvent(event);
            OrderPurchaseConfirmedMessage payload = event.payload();
            escrowReleaseUseCase.releaseEscrow(new EscrowReleaseCommand(
                    payload.orderId(),
                    payload.sellerMemberId(),
                    payload.confirmationType()
            ));
        } catch (Exception e) {
            log.error("Failed to process OrderPurchaseConfirmed event envelope", e);
            throw new RuntimeException("Failed to deserialize OrderPurchaseConfirmed event envelope", e);
        }
    }

    /**
     * 구매확정 이벤트 계약과 허용 confirmation type을 검증한다.
     */
    private void validateEvent(EventEnvelope<OrderPurchaseConfirmedMessage> event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("orderPurchaseConfirmed event is required.");
        }
        if (event.eventId() == null) {
            throw new InvalidOrderPaymentRequestException("eventId is required.");
        }
        if (!ORDER_PURCHASE_CONFIRMED_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidOrderPaymentRequestException("Unsupported eventType: " + event.eventType());
        }
        if (event.source() == null || event.source().isBlank()) {
            throw new InvalidOrderPaymentRequestException("source is required.");
        }
        if (event.aggregateId() == null) {
            throw new InvalidOrderPaymentRequestException("aggregateId is required.");
        }
        if (event.occurredAt() == null) {
            throw new InvalidOrderPaymentRequestException("occurredAt is required.");
        }
        if (event.traceId() == null || event.traceId().isBlank()) {
            throw new InvalidOrderPaymentRequestException("traceId is required.");
        }
        if (event.payload() == null) {
            throw new InvalidOrderPaymentRequestException("payload is required.");
        }
        if (event.payload().orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.payload().sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId is required.");
        }
        if (event.payload().confirmedAt() == null) {
            throw new InvalidOrderPaymentRequestException("confirmedAt is required.");
        }
        if (event.payload().confirmationType() == null) {
            throw new InvalidOrderPaymentRequestException("confirmationType is required.");
        }
        if (!Objects.equals(event.aggregateId(), event.payload().orderId())) {
            throw new InvalidOrderPaymentRequestException("aggregateId and payload.orderId must match.");
        }
    }
}
