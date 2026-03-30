package com.example.notification.application.service;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.common.exception.NotificationNotFoundException;
import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.repository.NotificationJpaRepository;
import com.example.notification.presentation.dto.NotificationResponse;
import com.example.notification.presentation.dto.NotificationUnreadCountResponse;
import com.example.notification.presentation.dto.PagedResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService implements NotificationUsecase {

    private final NotificationJpaRepository notificationJpaRepository;

    public NotificationService(NotificationJpaRepository notificationJpaRepository) {
        this.notificationJpaRepository = notificationJpaRepository;
    }

    // 알림 조회
    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(
        UUID memberId, 
        int page, 
        int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = notificationJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        var items = result.getContent().stream()
                .map(NotificationResponse::from)
                .toList();

        return new PagedResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    // 읽지 않은 알림 수 조회
    @Override
    public NotificationUnreadCountResponse getUnreadNotificationCount(UUID memberId) {
        return new NotificationUnreadCountResponse(notificationJpaRepository.countByMemberIdAndReadFalse(memberId));
    }

    // 알림 읽음 처리
    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID memberId, UUID notificationId) {
        Notification notification = notificationJpaRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getMemberId().equals(memberId)) {
            throw new NotificationNotFoundException(notificationId);
        }

        notification.markAsRead();
        return NotificationResponse.from(notification);
    }

    // 자동 구매확정 알림 생성
    @Override
    @Transactional
    public void createAutoPurchaseConfirmedNotification(
        UUID orderId, 
        UUID buyerMemberId, 
        LocalDateTime confirmedAt
    ) {
        // 필수 파라미터 검증
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (buyerMemberId == null) {
            throw new IllegalArgumentException("buyerMemberId is required.");
        }
        if (confirmedAt == null) {
            throw new IllegalArgumentException("confirmedAt is required.");
        }

        // 알림 생성
        Notification notification = Notification.create(
                UUID.randomUUID(),
                buyerMemberId,
                NotificationType.AUTO_PURCHASE_CONFIRMED,
                "자동 구매확정 완료",
                "주문이 자동으로 구매확정 처리되었습니다.",
                orderId,
                NotificationReferenceType.ORDER,
                confirmedAt
        );

        notificationJpaRepository.save(notification);
    }

    // 주문 결제 성공 알림 생성
    @Override
    @Transactional
    public void createOrderPaymentSucceededNotification(
            UUID orderId,
            UUID buyerMemberId,
            Long paidAmount,
            LocalDateTime occurredAt
    ) {
        validateOrderArguments(orderId, buyerMemberId, occurredAt);
        if (paidAmount == null || paidAmount <= 0) {
            throw new IllegalArgumentException("paidAmount must be positive.");
        }

        Notification notification = Notification.create(
                UUID.randomUUID(),
                buyerMemberId,
                NotificationType.ORDER_PAYMENT_SUCCEEDED,
                "Payment completed",
                "Your payment was completed successfully. Amount: " + paidAmount,
                orderId,
                NotificationReferenceType.ORDER,
                occurredAt
        );

        notificationJpaRepository.save(notification);
    }

    // 주문 결제 실패 알림 생성
    @Override
    @Transactional
    public void createOrderPaymentFailedNotification(
            UUID orderId,
            UUID buyerMemberId,
            OrderPaymentFailureReason failureReason,
            LocalDateTime occurredAt
    ) {
        validateOrderArguments(orderId, buyerMemberId, occurredAt);
        if (failureReason == null) {
            throw new IllegalArgumentException("failureReason is required.");
        }

        Notification notification = Notification.create(
                UUID.randomUUID(),
                buyerMemberId,
                NotificationType.ORDER_PAYMENT_FAILED,
                "Payment failed",
                mapFailureReasonToContent(failureReason),
                orderId,
                NotificationReferenceType.ORDER,
                occurredAt
        );

        notificationJpaRepository.save(notification);
    }

    private void validateOrderArguments(UUID orderId, UUID buyerMemberId, LocalDateTime occurredAt) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (buyerMemberId == null) {
            throw new IllegalArgumentException("buyerMemberId is required.");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt is required.");
        }
    }

    private String mapFailureReasonToContent(OrderPaymentFailureReason failureReason) {
        return switch (failureReason) {
            case INSUFFICIENT_BALANCE -> "Payment failed due to insufficient balance.";
            case WALLET_NOT_FOUND -> "Payment failed because no wallet information was found.";
            case INVALID_REQUEST -> "Payment failed because the request was invalid.";
            case DUPLICATE_ORDER_PAYMENT -> "Payment was already processed for this order.";
            case INTERNAL_ERROR -> "Payment failed due to a temporary internal error.";
        };
    }
}
