package com.example.notification.presentation.dto;

import java.util.UUID;

public record NotificationAction(
        String label,
        String actionType,
        String routeKey,
        UUID referenceId,
        String variant
) {
}
