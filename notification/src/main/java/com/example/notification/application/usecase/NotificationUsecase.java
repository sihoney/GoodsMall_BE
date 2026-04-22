package com.example.notification.application.usecase;

import com.example.notification.application.dto.NotificationCommand;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.notification.presentation.dto.NotificationResponse;
import com.example.notification.presentation.dto.NotificationUnreadCountResponse;
import com.example.notification.presentation.dto.PagedResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationUsecase {

    PagedResponse<NotificationResponse> getMyNotifications(UUID memberId, int page, int size);

    NotificationUnreadCountResponse getUnreadNotificationCount(UUID memberId);

    NotificationResponse markAsRead(UUID memberId, UUID notificationId);

    void createNotification(NotificationCommand command);

    void createAutoPurchaseConfirmedNotification(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            LocalDateTime confirmedAt
    );

    void createOrderPaymentSucceededNotification(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            Long paidAmount,
            LocalDateTime occurredAt
    );

    void createOrderPaymentFailedNotification(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            OrderPaymentFailureReason failureReason,
            LocalDateTime occurredAt
    );

    void createSellerSettlementPayoutSucceededNotification(
            UUID eventId,
            String traceId,
            UUID settlementId,
            UUID sellerMemberId,
            Long payoutAmount,
            LocalDateTime processedAt
    );

    void createSellerSettlementPayoutFailedNotification(
            UUID eventId,
            String traceId,
            UUID settlementId,
            UUID sellerMemberId,
            PayoutFailureReason failureReason,
            LocalDateTime processedAt
    );
}
