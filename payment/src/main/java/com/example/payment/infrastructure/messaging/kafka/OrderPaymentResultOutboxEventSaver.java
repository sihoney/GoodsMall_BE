package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.OutboxEventPendingTrigger;
import com.example.payment.domain.entity.OutboxEvent;
import com.example.payment.domain.repository.OutboxRepository;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 주문 결제 결과 이벤트를 payment outbox에 저장한다.
 */
@Slf4j
@Component
public class OrderPaymentResultOutboxEventSaver {

    private static final String ORDER_PAYMENT_RESULT_EVENT_TYPE = "ORDER_PAYMENT_RESULT";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderPaymentResultOutboxEventSaver(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void save(OrderPaymentResultMessage event) {
        try {
            EventEnvelope<OrderPaymentResultMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    ORDER_PAYMENT_RESULT_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.orderId(),
                    event.buyerMemberId(),
                    event.occurredAt(),
                    resolveTraceId(event),
                    event
            );
            String message = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    KafkaTopics.ORDER_PAYMENT_RESULT,
                    ORDER_PAYMENT_RESULT_EVENT_TYPE,
                    String.valueOf(event.orderId()),
                    resolveTraceId(event),
                    message
            );
            outboxRepository.save(outboxEvent);
            applicationEventPublisher.publishEvent(new OutboxEventPendingTrigger());
        } catch (Exception e) {
            log.error("Failed to serialize OrderPaymentResultMessage. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to serialize OrderPaymentResultMessage", e);
        }
    }

    private String resolveTraceId(OrderPaymentResultMessage event) {
        UUID eventId = event.eventId();
        if (eventId == null) {
            return "payment-order-payment-result";
        }
        return eventId.toString();
    }
}
