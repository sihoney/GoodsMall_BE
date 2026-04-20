package com.example.notification.infrastructure.messaging.kafka.consumer;

import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationConsumerExceptionClassifier;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationConsumerFailureAction;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationConsumerFailureDecision;
import com.example.notification.infrastructure.messaging.kafka.dlq.NotificationDlqPublisher;
import com.example.notification.infrastructure.messaging.kafka.dlq.EventParseException;
import com.example.notification.infrastructure.messaging.kafka.handler.NotificationEventHandler;
import com.example.notification.infrastructure.messaging.kafka.handler.NotificationEventHandlerRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final String UNIFIED_NOTIFICATION_LISTENER = "listenNotificationEvent";

    private static final TypeReference<EventEnvelope<JsonNode>> GENERIC_EVENT_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;
    private final NotificationEventHandlerRegistry handlerRegistry;
    private final NotificationConsumerExceptionClassifier exceptionClassifier;
    private final NotificationDlqPublisher notificationDlqPublisher;

    @KafkaListener(
            topics = {
                    "${notification.kafka.topics.member-signed-up:member-signed-up}",
                    "${notification.kafka.topics.order-payment-result:payment.order-payment-result}"
            },
            groupId = "${notification.kafka.consumer-groups.member-signed-up:notification-service}",
            containerFactory = "memberSignedUpKafkaListenerContainerFactory"
    )
    public void listen(String message) {
        consume(UNIFIED_NOTIFICATION_LISTENER, message, () -> {
            EventEnvelope<JsonNode> envelope = parseEnvelope(message);
            NotificationEventHandler handler = handlerRegistry.get(envelope.eventType());
            handler.handle(envelope);
        });
    }

    private void consume(String listenerName, String rawMessage, Runnable task) {
        try {
            task.run();
        } catch (RuntimeException exception) {
            handleConsumerFailure(listenerName, rawMessage, exception);
        }
    }

    private void handleConsumerFailure(String listenerName, String rawMessage, RuntimeException exception) {
        NotificationConsumerFailureDecision decision = exceptionClassifier.classify(exception);

        if (decision.action() == NotificationConsumerFailureAction.DLQ) {
            notificationDlqPublisher.publish(listenerName, rawMessage, exception, decision);
            return;
        }

        if (decision.action() == NotificationConsumerFailureAction.IGNORE) {
            return;
        }

        throw exception;
    }

    private EventEnvelope<JsonNode> parseEnvelope(String message) {
        try {
            return objectMapper.readValue(message, GENERIC_EVENT_ENVELOPE_TYPE);
        } catch (JsonProcessingException e) {
            throw new EventParseException("Failed to parse notification event envelope.", e);
        }
    }
}
