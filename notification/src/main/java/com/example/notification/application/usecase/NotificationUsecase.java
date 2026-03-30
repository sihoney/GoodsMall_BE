package com.example.notification.application.usecase;

import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.presentation.dto.NotificationResponse;
import com.example.notification.presentation.dto.NotificationUnreadCountResponse;
import com.example.notification.presentation.dto.PagedResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationUsecase {

    PagedResponse<NotificationResponse> getMyNotifications(UUID memberId, int page, int size);

    NotificationUnreadCountResponse getUnreadNotificationCount(UUID memberId);

    NotificationResponse markAsRead(UUID memberId, UUID notificationId);

    void createAutoPurchaseConfirmedNotification(UUID orderId, UUID buyerMemberId, LocalDateTime confirmedAt);

    void createOrderPaymentSucceededNotification(
            UUID orderId,
            UUID buyerMemberId,
            Long paidAmount,
            LocalDateTime occurredAt
    );

    void createOrderPaymentFailedNotification(
            UUID orderId,
            UUID buyerMemberId,
            OrderPaymentFailureReason failureReason,
            LocalDateTime occurredAt
    );
}
