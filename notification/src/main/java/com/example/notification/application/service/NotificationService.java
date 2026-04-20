package com.example.notification.application.service;

import com.example.notification.application.dto.NotificationCommand;
import com.example.notification.application.monitoring.NotificationMetricsRecorder;
import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.common.exception.NotificationNotFoundException;
import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationMetricReason;
import com.example.notification.domain.enumtype.NotificationReferenceType;
import com.example.notification.domain.enumtype.NotificationStatus;
import com.example.notification.domain.enumtype.NotificationType;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.notification.infrastructure.repository.NotificationJpaRepository;
import com.example.notification.presentation.dto.NotificationResponse;
import com.example.notification.presentation.dto.NotificationUnreadCountResponse;
import com.example.notification.presentation.dto.PagedResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService implements NotificationUsecase {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationPushService notificationPushService;
    private final NotificationMetricsRecorder notificationMetricsRecorder;

    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(UUID memberId, int page, int size) {
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

    @Override
    public NotificationUnreadCountResponse getUnreadNotificationCount(UUID memberId) {
        return new NotificationUnreadCountResponse(notificationJpaRepository.countByMemberIdAndReadFalse(memberId));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID memberId, UUID notificationId) {
        Notification notification = notificationJpaRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getMemberId().equals(memberId)) {
            throw new NotificationNotFoundException(notificationId);
        }

        boolean alreadyRead = notification.isRead();
        notification.markAsRead();
        notificationMetricsRecorder.recordMarkRead(alreadyRead); // 이미 읽은 알림인지 여부 기록
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public void createNotification(NotificationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("notification command is required.");
        }
        validateCommonArguments(command.eventId(), command.memberId(), command.occurredAt());
        if (command.type() == null) {
            throw new IllegalArgumentException("type is required.");
        }
        if (command.title() == null || command.title().isBlank()) {
            throw new IllegalArgumentException("title is required.");
        }
        if (command.content() == null || command.content().isBlank()) {
            throw new IllegalArgumentException("content is required.");
        }
        saveNotification(
                command.eventId(),
                command.traceId(),
                command.memberId(),
                command.type(),
                command.title(),
                command.content(),
                command.referenceId(),
                command.referenceType(),
                command.occurredAt()
        );
    }

    @Override
    @Transactional
    public void createAutoPurchaseConfirmedNotification(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            LocalDateTime confirmedAt
    ) {
        validateCommonArguments(eventId, buyerMemberId, confirmedAt);
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required.");
        }

        saveNotification(
                eventId,
                traceId,
                buyerMemberId,
                NotificationType.AUTO_PURCHASE_CONFIRMED,
                "Auto purchase confirmed",
                "Your order was automatically confirmed.",
                orderId,
                NotificationReferenceType.ORDER,
                confirmedAt
        );
    }

    @Override
    @Transactional
    public void createOrderPaymentSucceededNotification(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            Long paidAmount,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, buyerMemberId, occurredAt);
        validateOrderArguments(orderId, paidAmount);

        saveNotification(
                eventId,
                traceId,
                buyerMemberId,
                NotificationType.ORDER_PAYMENT_SUCCEEDED,
                "Payment completed",
                "Your payment was completed successfully. Amount: " + paidAmount,
                orderId,
                NotificationReferenceType.ORDER,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createOrderPaymentFailedNotification(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            OrderPaymentFailureReason failureReason,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, buyerMemberId, occurredAt);
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (failureReason == null) {
            throw new IllegalArgumentException("failureReason is required.");
        }

        saveNotification(
                eventId,
                traceId,
                buyerMemberId,
                NotificationType.ORDER_PAYMENT_FAILED,
                "Payment failed",
                mapFailureReasonToContent(failureReason),
                orderId,
                NotificationReferenceType.ORDER,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createSellerSettlementPayoutSucceededNotification(
            UUID eventId,
            String traceId,
            UUID settlementId,
            UUID sellerMemberId,
            Long payoutAmount,
            LocalDateTime processedAt
    ) {
        validateCommonArguments(eventId, sellerMemberId, processedAt);
        if (settlementId == null) {
            throw new IllegalArgumentException("settlementId is required.");
        }
        if (payoutAmount == null || payoutAmount <= 0) {
            throw new IllegalArgumentException("payoutAmount must be positive.");
        }

        saveNotification(
                eventId,
                traceId,
                sellerMemberId,
                NotificationType.SELLER_SETTLEMENT_PAYOUT_SUCCEEDED,
                "Settlement payout completed",
                "Your settlement payout was completed. Amount: " + payoutAmount,
                settlementId,
                NotificationReferenceType.SETTLEMENT,
                processedAt
        );
    }

    @Override
    @Transactional
    public void createSellerSettlementPayoutFailedNotification(
            UUID eventId,
            String traceId,
            UUID settlementId,
            UUID sellerMemberId,
            PayoutFailureReason failureReason,
            LocalDateTime processedAt
    ) {
        validateCommonArguments(eventId, sellerMemberId, processedAt);
        if (settlementId == null) {
            throw new IllegalArgumentException("settlementId is required.");
        }
        if (failureReason == null) {
            throw new IllegalArgumentException("failureReason is required.");
        }

        saveNotification(
                eventId,
                traceId,
                sellerMemberId,
                NotificationType.SELLER_SETTLEMENT_PAYOUT_FAILED,
                "Settlement payout failed",
                mapPayoutFailureReasonToContent(failureReason),
                settlementId,
                NotificationReferenceType.SETTLEMENT,
                processedAt
        );
    }

    private void saveNotification(
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
        notificationMetricsRecorder.recordEventReceived(type); // 알림 이벤트 수신 카운트 기록

        if (notificationJpaRepository.existsByEventId(eventId)) {
            notificationMetricsRecorder.recordDuplicateEvent(type); // 중복 이벤트 카운트 기록
            log.info("Duplicate notification ignored. eventId={} memberId={} type={}", eventId, memberId, type);
            return;
        }

        try {
            Notification notification = Notification.create(
                    UUID.randomUUID(),
                    eventId,
                    traceId,
                    memberId,
                    type,
                    title,
                    content,
                    referenceId,
                    referenceType,
                    NotificationStatus.STORED,
                    occurredAt
            );

            Notification savedNotification = notificationJpaRepository.save(notification);
            notificationMetricsRecorder.recordSaved(type); // 저장된 알림 수 기록
            pushAfterCommit(NotificationResponse.from(savedNotification));
        } catch (RuntimeException e) {
            notificationMetricsRecorder.recordSaveFailed(type, NotificationMetricReason.DB_ERROR.name()); // 저장 실패 카운트 기록
            throw e;
        }
    }

    private void pushAfterCommit(NotificationResponse notificationResponse) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationPushService.push(notificationResponse);
                }
            });
            return;
        }

        notificationPushService.push(notificationResponse);
    }

    private void validateCommonArguments(UUID eventId, UUID memberId, LocalDateTime occurredAt) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("memberId is required.");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt is required.");
        }
    }

    private void validateOrderArguments(UUID orderId, Long paidAmount) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (paidAmount == null || paidAmount <= 0) {
            throw new IllegalArgumentException("paidAmount must be positive.");
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

    private String mapPayoutFailureReasonToContent(PayoutFailureReason failureReason) {
        return switch (failureReason) {
            case WALLET_NOT_FOUND -> "Settlement payout failed because no wallet information was found.";
            case INVALID_PAYOUT_AMOUNT -> "Settlement payout failed because the payout amount was invalid.";
            case DUPLICATE_PAYOUT -> "Settlement payout was already processed for this settlement.";
            case SETTLEMENT_NOT_FOUND -> "Settlement payout failed because the settlement could not be found.";
            case TEMPORARY_DB_ERROR -> "Settlement payout failed due to a temporary database error.";
            case KAFKA_PUBLISH_ERROR -> "Settlement payout failed during event publishing.";
            case INTERNAL_ERROR -> "Settlement payout failed due to a temporary internal error.";
        };
    }
}
