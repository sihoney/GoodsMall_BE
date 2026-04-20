package com.example.notification.infrastructure.messaging.kafka.dlq;

public interface NotificationDlqPublisher {

    void publish(
            String listenerName,
            String rawMessage,
            RuntimeException exception,
            NotificationConsumerFailureDecision decision
    );
}
