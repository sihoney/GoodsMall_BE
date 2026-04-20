package com.example.notification.infrastructure.messaging.kafka.dlq;

public interface NotificationConsumerExceptionClassifier {

    NotificationConsumerFailureDecision classify(RuntimeException exception);
}
