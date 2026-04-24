package com.example.notification.infrastructure.messaging.kafka.dlq.classifier;

import com.example.notification.infrastructure.messaging.kafka.dlq.exception.EventParseException;
import com.example.notification.infrastructure.messaging.kafka.dlq.exception.InvalidEventPayloadException;
import com.example.notification.infrastructure.messaging.kafka.dlq.exception.UnsupportedEventTypeException;
import com.example.notification.infrastructure.messaging.kafka.dlq.model.NotificationConsumerFailureDecision;
import com.example.notification.infrastructure.messaging.kafka.dlq.model.NotificationDlqReason;
import org.springframework.stereotype.Component;

@Component
public class DefaultNotificationConsumerExceptionClassifier implements NotificationConsumerExceptionClassifier {

    @Override
    public NotificationConsumerFailureDecision classify(RuntimeException exception) {
        if (exception instanceof EventParseException) {
            return NotificationConsumerFailureDecision.dlq(NotificationDlqReason.EVENT_PARSE_FAILURE);
        }

        if (exception instanceof UnsupportedEventTypeException) {
            return NotificationConsumerFailureDecision.dlq(NotificationDlqReason.UNSUPPORTED_EVENT_TYPE);
        }

        if (exception instanceof InvalidEventPayloadException) {
            return NotificationConsumerFailureDecision.dlq(NotificationDlqReason.INVALID_EVENT_PAYLOAD);
        }

        if (exception instanceof IllegalArgumentException) {
            return NotificationConsumerFailureDecision.dlq(NotificationDlqReason.INVALID_EVENT_PAYLOAD);
        }

        return NotificationConsumerFailureDecision.retry(NotificationDlqReason.TEMPORARY_PROCESSING_ERROR);
    }
}
