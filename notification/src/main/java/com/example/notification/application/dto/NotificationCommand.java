package com.example.notification.application.dto;

import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationCommand(
        UUID eventId,
        String traceId,
        UUID memberId,
        NotificationType type,
        String title,
        String content,
        UUID referenceId,
        NotificationReferenceType referenceType,
        LocalDateTime occurredAt
) {
    // TODO: 모든 producer가 공통 EventEnvelope + payload로 통일되면 이 내부 정규화 DTO를 제거하고,
    // NotificationService가 EventEnvelope를 직접 받도록 단순화할 수 있다.
}
