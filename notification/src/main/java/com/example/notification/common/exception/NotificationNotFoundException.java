package com.example.notification.common.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID notificationId) {
        super("알림을 찾을 수 없습니다. notificationId=" + notificationId);
    }
}
