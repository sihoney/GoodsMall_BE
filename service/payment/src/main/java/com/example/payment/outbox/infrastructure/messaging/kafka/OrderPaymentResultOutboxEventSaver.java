package com.example.payment.outbox.infrastructure.messaging.kafka;


import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.outbox.application.event.OutboxEventPendingTrigger;
import com.example.payment.outbox.domain.entity.OutboxEvent;
import com.example.payment.outbox.domain.repository.OutboxRepository;
import com.example.payment.common.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 二쇰Ц 寃곗젣 寃곌낵 ?대깽?몃? payment outbox????ν븳??
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
            log.error("OrderPaymentResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎. orderId={}", event.orderId(), e);
            throw new RuntimeException("OrderPaymentResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", e);
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
