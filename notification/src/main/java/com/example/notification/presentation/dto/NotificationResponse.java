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
            case BUYER_SIGNUP_COMPLETED,
                 SELLER_PROMOTED,
                 ACCOUNT_VERIFICATION_EXPIRED,
                 ACCOUNT_VERIFICATION_FAILED,
                 MEMBER_OAUTH_LINKED -> null;
            case BUYER_ORDER_CREATED,
                 BUYER_ORDER_CANCELED,
                 BUYER_AUTO_PURCHASE_CONFIRMED,
                 BUYER_ORDER_PAYMENT_SUCCEEDED,
                 BUYER_ORDER_PAYMENT_FAILED,
                 SELLER_ORDER_RECEIVED,
                 SELLER_ORDER_CANCELED -> "주문번호 " + notification.getReferenceId();
            case SELLER_SETTLEMENT_PAYOUT_SUCCEEDED,
                 SELLER_SETTLEMENT_PAYOUT_FAILED -> "정산번호 " + notification.getReferenceId();
            case BUYER_AUCTION_OUTBID,
                 BUYER_AUCTION_WON,
                 SELLER_AUCTION_CLOSED_SOLD,
                 SELLER_AUCTION_CLOSED_UNSOLD -> "경매번호 " + notification.getReferenceId();
        };
    }

    private static List<NotificationAction> resolveActions(Notification notification) {
        UUID referenceId = notification.getReferenceId();

        return switch (notification.getType()) {
            case BUYER_SIGNUP_COMPLETED -> List.of(
                    new NotificationAction("로그인", "navigate", "LOGIN", null, "primary"),
                    new NotificationAction("내 정보 보기", "navigate", "MY_PROFILE", null, "secondary")
            );
            case SELLER_PROMOTED -> List.of(
                    new NotificationAction("판매자 홈", "navigate", "SELLER_DASHBOARD", null, "primary"),
                    new NotificationAction("내 정보", "navigate", "MY_PROFILE", null, "secondary")
            );
            case ACCOUNT_VERIFICATION_EXPIRED,
                 ACCOUNT_VERIFICATION_FAILED -> List.of(
                    new NotificationAction("다시 인증", "navigate", "SELLER_VERIFICATION", null, "primary"),
                    new NotificationAction("내 정보", "navigate", "MY_PROFILE", null, "secondary")
            );
            case MEMBER_OAUTH_LINKED -> List.of(
                    new NotificationAction("연동 계정 보기", "navigate", "MY_OAUTH_ACCOUNTS", null, "primary"),
                    new NotificationAction("내 정보", "navigate", "MY_PROFILE", null, "secondary")
            );
            case BUYER_ORDER_CREATED -> List.of(
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("주문 내역", "ORDER_HISTORY", null, "secondary")
            );
            case BUYER_ORDER_CANCELED -> List.of(
                    createNavigateAction("주문 내역", "ORDER_HISTORY", null, "primary"),
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "secondary")
            );
            case BUYER_AUTO_PURCHASE_CONFIRMED -> List.of(
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("배송 조회", "DELIVERY_DETAIL", referenceId, "secondary")
            );
            case BUYER_ORDER_PAYMENT_SUCCEEDED -> List.of(
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("배송 조회", "DELIVERY_DETAIL", referenceId, "secondary")
            );
            case BUYER_ORDER_PAYMENT_FAILED -> List.of(
                    createNavigateAction("다시 시도", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("지갑 충전", "WALLET_TOPUP", null, "secondary")
            );
            case SELLER_ORDER_RECEIVED -> List.of(
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "primary"),
                    createNavigateAction("주문 관리", "SELLER_ORDER_MANAGEMENT", null, "secondary")
            );
            case SELLER_ORDER_CANCELED -> List.of(
                    createNavigateAction("주문 관리", "SELLER_ORDER_MANAGEMENT", null, "primary"),
                    createNavigateAction("주문 상세", "ORDER_DETAIL", referenceId, "secondary")
            );
            case SELLER_SETTLEMENT_PAYOUT_SUCCEEDED -> List.of(
                    createNavigateAction("정산 내역", "SETTLEMENT_DETAIL", referenceId, "primary"),
                    createNavigateAction("거래 내역", "SELLER_TRANSACTION_HISTORY", referenceId, "secondary")
            );
            case SELLER_SETTLEMENT_PAYOUT_FAILED -> List.of(
                    createNavigateAction("다시 요청", "SETTLEMENT_DETAIL", referenceId, "primary"),
                    new NotificationAction("고객센터 문의", "callback", "SUPPORT_CONTACT", null, "secondary")
            );
            case BUYER_AUCTION_OUTBID -> List.of(
                    createNavigateAction("다시 입찰", "AUCTION_DETAIL", referenceId, "primary"),
                    createNavigateAction("경매 목록", "AUCTION_LIST", null, "secondary")
            );
            case BUYER_AUCTION_WON -> List.of(
                    createNavigateAction("경매 상세", "AUCTION_DETAIL", referenceId, "primary"),
                    createNavigateAction("결제 진행", "AUCTION_PAYMENT", referenceId, "secondary")
            );
            case SELLER_AUCTION_CLOSED_SOLD -> List.of(
                    createNavigateAction("경매 상세", "AUCTION_DETAIL", referenceId, "primary"),
                    createNavigateAction("판매 관리", "SELLER_AUCTION_MANAGEMENT", null, "secondary")
            );
            case SELLER_AUCTION_CLOSED_UNSOLD -> List.of(
                    createNavigateAction("경매 상세", "AUCTION_DETAIL", referenceId, "primary"),
                    createNavigateAction("다시 등록", "SELLER_AUCTION_CREATE", null, "secondary")
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
