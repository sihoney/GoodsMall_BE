package com.example.notification.application.usecase;

import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.notification.presentation.dto.NotificationResponse;
import com.example.notification.presentation.dto.NotificationUnreadCountResponse;
import com.example.notification.presentation.dto.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationUsecase {

    PagedResponse<NotificationResponse> getMyNotifications(UUID memberId, int page, int size);

    NotificationUnreadCountResponse getUnreadNotificationCount(UUID memberId);

    NotificationResponse markAsRead(UUID memberId, UUID notificationId);

    void createMemberSignedUpNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    );

    void createSellerPromotedNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    );

    void createAccountVerificationExpiredNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    );

    void createAccountVerificationFailedNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    );

    void createMemberOauthLinkedNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            String provider,
            LocalDateTime occurredAt
    );

    void createOrderCreatedNotifications(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            Long totalAmount,
            List<UUID> sellerMemberIds,
            LocalDateTime occurredAt
    );

    void createOrderCanceledNotifications(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            List<UUID> sellerMemberIds,
            LocalDateTime occurredAt
    );

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

    void createAuctionOutbidNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID outbidBidderId,
            LocalDateTime occurredAt
    );

    void createAuctionWonNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID winnerMemberId,
            String auctionTitle,
            Long finalPrice,
            LocalDateTime occurredAt
    );

    void createAuctionClosedSoldNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID sellerMemberId,
            String auctionTitle,
            Long finalPrice,
            LocalDateTime occurredAt
    );

    void createAuctionClosedUnsoldNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID sellerMemberId,
            String auctionTitle,
            LocalDateTime occurredAt
    );
}
