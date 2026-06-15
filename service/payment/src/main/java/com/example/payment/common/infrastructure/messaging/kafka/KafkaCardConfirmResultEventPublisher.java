package com.example.payment.common.infrastructure.messaging.kafka;

import com.example.payment.common.domain.service.CardConfirmResultEventPublisher;
import com.example.payment.common.infrastructure.messaging.kafka.contract.CardConfirmResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class KafkaCardConfirmResultEventPublisher implements CardConfirmResultEventPublisher {

    private static final String CARD_CONFIRM_RESULT_EVENT_TYPE = "CARD_CONFIRM_RESULT";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaCardConfirmResultEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(CardConfirmResultMessage event) {
        try {
            EventEnvelope<CardConfirmResultMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    CARD_CONFIRM_RESULT_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.orderId(),
                    null,
                    event.occurredAt(),
                    resolveTraceId(event),
                    event
            );
            String message = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.CARD_CONFIRM_RESULT, String.valueOf(event.orderId()), message);
        } catch (Exception e) {
            log.error("CardConfirmResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎. orderId={}", event.orderId(), e);
            throw new RuntimeException("CardConfirmResultMessage 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    private String resolveTraceId(CardConfirmResultMessage event) {
        UUID eventId = event.eventId();
        if (eventId == null) {
            return "payment-card-confirm-result";
        }
        return eventId.toString();
    }
}
