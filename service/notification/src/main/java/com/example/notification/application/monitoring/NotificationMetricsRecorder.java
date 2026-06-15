package com.example.notification.application.monitoring;

import com.example.notification.domain.enumtype.NotificationType;

public interface NotificationMetricsRecorder {

    void recordEventReceived(NotificationType type);

    void recordDuplicateEvent(NotificationType type);

    void recordSaved(NotificationType type);

    void recordSaveFailed(NotificationType type, String reason);

    void recordPushAttempt(NotificationType type);

    void recordPushSuccess(NotificationType type);

    void recordPushFailure(NotificationType type, String reason);

    void recordEmitterMissing(NotificationType type);

    void recordPushSkippedAlreadyPushed(NotificationType type);

    void recordMarkRead(boolean noop);
}
