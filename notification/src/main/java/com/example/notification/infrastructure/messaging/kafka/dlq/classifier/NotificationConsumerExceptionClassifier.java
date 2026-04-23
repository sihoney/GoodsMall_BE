package com.example.notification.infrastructure.messaging.kafka.dlq.classifier;

import com.example.notification.infrastructure.messaging.kafka.dlq.model.NotificationConsumerFailureDecision;

public interface NotificationConsumerExceptionClassifier {

    NotificationConsumerFailureDecision classify(RuntimeException exception);
}
