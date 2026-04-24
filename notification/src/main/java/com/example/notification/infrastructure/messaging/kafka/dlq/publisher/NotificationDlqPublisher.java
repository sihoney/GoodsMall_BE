package com.example.notification.infrastructure.messaging.kafka.dlq.publisher;

import com.example.notification.infrastructure.messaging.kafka.dlq.model.NotificationConsumerFailureDecision;

public interface NotificationDlqPublisher {

    void publish(
            String listenerName,
            String rawMessage,
            RuntimeException exception,
            NotificationConsumerFailureDecision decision
    );
}
