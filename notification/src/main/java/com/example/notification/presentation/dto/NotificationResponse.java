package com.example.notification.presentation.dto;

import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        @JsonIgnore UUID memberId,
        NotificationType type,
        String title,
        String subtitle,
        String content,
        List<NotificationAction> actions,
        UUID referenceId,
        NotificationReferenceType referenceType,
        boolean read,
        LocalDateTime createdAt,
        String elapsedTime,
        UUID eventId,
        String traceId
) {

    public static NotificationResponse from(Notification notification) {
        return from(notification, LocalDateTime.now());
    }

    public static NotificationResponse from(Notification notification, LocalDateTime now) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getMemberId(),
                notification.getType(),
                notification.getTitle(),
                resolveSubtitle(notification),
                notification.getContent(),
                resolveActions(notification),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.isRead(),
                notification.getCreatedAt(),
                formatElapsedTime(notification.getCreatedAt(), now),
                notification.getEventId(),
                notification.getTraceId()
        );
    }

    private static String resolveSubtitle(Notification notification) {
        if (notification.getReferenceId() == null) {
            return null;
        }

        return switch (notification.getType()) {
            case MEMBER_SIGNED_UP -> null;
            case AUTO_PURCHASE_CONFIRMED, ORDER_PAYMENT_SUCCEEDED, ORDER_PAYMENT_FAILED ->
                    "주문번호 " + notification.getReferenceId();
            case SELLER_SETTLEMENT_PAYOUT_SUCCEEDED, SELLER_SETTLEMENT_PAYOUT_FAILED ->
                    "정산번호 " + notification.getReferenceId();
            case AUCTION_BID_OUTBID -> "경매번호 " + notification.getReferenceId();
        };
    }

    private static List<NotificationAction> resolveActions(Notification notification) {
        UUID referenceId = notification.getReferenceId();
        return switch (notification.getType()) {
            case MEMBER_SIGNED_UP -> List.of(
                    new NotificationAction("로그인", "navigate", "LOGIN", null, "primary"),
                    new NotificationAction("내 정보 보기", "navigate", "MY_PROFILE", null, "secondary")
            );
            case AUTO_PURCHASE_CONFIRMED -> List.of(
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("배송 조회", "DELIVERY_DETAIL", referenceId, "secondary")
            );
            case ORDER_PAYMENT_SUCCEEDED -> List.of(
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("배송 조회", "DELIVERY_DETAIL", referenceId, "secondary")
            );
            case ORDER_PAYMENT_FAILED -> List.of(
                    createNavigateAction("다시 시도", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("지갑 충전", "WALLET_TOPUP", null, "secondary")
            );
            case SELLER_SETTLEMENT_PAYOUT_SUCCEEDED -> List.of(
                    createNavigateAction("정산 내역", "SETTLEMENT_DETAIL", referenceId, "primary"),
                    createNavigateAction("거래 내역", "SELLER_TRANSACTION_HISTORY", referenceId, "secondary")
            );
            case SELLER_SETTLEMENT_PAYOUT_FAILED -> List.of(
                    createNavigateAction("다시 요청", "SETTLEMENT_DETAIL", referenceId, "primary"),
                    new NotificationAction("고객센터 문의", "callback", "SUPPORT_CONTACT", null, "secondary")
            );
            case AUCTION_BID_OUTBID -> List.of(
                    createNavigateAction("경매 보기", "AUCTION_DETAIL", referenceId, "primary")
            );
        };
    }

    private static NotificationAction createNavigateAction(
            String label,
            String routeKey,
            UUID referenceId,
            String variant
    ) {
        return new NotificationAction(label, "navigate", routeKey, referenceId, variant);
    }

    private static String formatElapsedTime(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null || now == null || now.isBefore(createdAt)) {
            return null;
        }

        Duration duration = Duration.between(createdAt, now);
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            return "방금 전";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = duration.toDays();
        return days + "일 전";
    }
}
