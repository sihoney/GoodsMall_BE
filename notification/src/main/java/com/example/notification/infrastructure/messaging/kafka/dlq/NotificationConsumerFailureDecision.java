package com.example.notification.infrastructure.messaging.kafka.dlq;

import java.util.Objects;

public record NotificationConsumerFailureDecision(
        NotificationConsumerFailureAction action,
        NotificationDlqReason reason
) {
    public NotificationConsumerFailureDecision {
        Objects.requireNonNull(action, "action is required.");
        Objects.requireNonNull(reason, "reason is required.");
    }

    public static NotificationConsumerFailureDecision dlq(NotificationDlqReason reason) {
        return new NotificationConsumerFailureDecision(NotificationConsumerFailureAction.DLQ, reason);
    }

    public static NotificationConsumerFailureDecision retry(NotificationDlqReason reason) {
        return new NotificationConsumerFailureDecision(NotificationConsumerFailureAction.RETRY, reason);
    }

    public static NotificationConsumerFailureDecision ignore(NotificationDlqReason reason) {
        return new NotificationConsumerFailureDecision(NotificationConsumerFailureAction.IGNORE, reason);
    }
}
