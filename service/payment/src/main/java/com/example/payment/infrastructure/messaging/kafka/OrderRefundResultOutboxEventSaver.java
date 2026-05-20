package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.OutboxEventPendingTrigger;
import com.example.payment.domain.entity.OutboxEvent;
import com.example.payment.domain.repository.OutboxRepository;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderRefundResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 주문 환불 결과 이벤트를 payment outbox에 저장한다.
 */
@Slf4j
@Component
public class OrderRefundResultOutboxEventSaver {

    private static final String ORDER_REFUND_RESULT_EVENT_TYPE = "ORDER_REFUND_RESULT";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderRefundResultOutboxEventSaver(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void save(OrderRefundResultMessage event) {
        try {
            EventEnvelope<OrderRefundResultMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    ORDER_REFUND_RESULT_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.orderId(),
                    null,
                    event.occurredAt(),
                    resolveTraceId(event),
                    event
            );
            String message = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    KafkaTopics.ORDER_REFUND_RESULT,
                    ORDER_REFUND_RESULT_EVENT_TYPE,
                    String.valueOf(event.orderId()),
                    resolveTraceId(event),
                    message
            );
            outboxRepository.save(outboxEvent);
            applicationEventPublisher.publishEvent(new OutboxEventPendingTrigger());
        } catch (Exception e) {
            log.error("OrderRefundResultMessage 직렬화에 실패했습니다. orderId={}", event.orderId(), e);
            throw new RuntimeException("OrderRefundResultMessage 직렬화에 실패했습니다.", e);
        }
    }

    private String resolveTraceId(OrderRefundResultMessage event) {
        UUID refundId = event.refundId();
        if (refundId != null) {
            return refundId.toString();
        }
        UUID eventId = event.eventId();
        if (eventId != null) {
            return eventId.toString();
        }
        return "payment-order-refund-result";
    }
}
