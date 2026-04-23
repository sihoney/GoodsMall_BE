package com.example.notification.application.service;

import com.example.notification.application.monitoring.NotificationMetricsRecorder;
import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.common.exception.NotificationNotFoundException;
import com.example.notification.domain.entity.Notification;
import com.example.notification.domain.enumtype.NotificationChannel;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        notificationMetricsRecorder.recordMarkRead(alreadyRead);
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public void createMemberSignedUpNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, memberId, occurredAt);
        saveNotification(
                eventId,
                traceId,
                memberId,
                NotificationType.BUYER_SIGNUP_COMPLETED,
                "회원가입을 환영해요",
                "투데이런치 회원가입이 완료되었어요.",
                null,
                null,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createSellerPromotedNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, memberId, occurredAt);
        saveNotification(
                eventId,
                traceId,
                memberId,
                NotificationType.SELLER_PROMOTED,
                "판매자 자격이 완료되었어요",
                "이제 판매자 기능을 이용할 수 있어요.",
                null,
                null,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createAccountVerificationExpiredNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, memberId, occurredAt);
        saveNotification(
                eventId,
                traceId,
                memberId,
                NotificationType.ACCOUNT_VERIFICATION_EXPIRED,
                "계좌 인증이 만료되었어요",
                "다시 계좌 인증을 진행해 주세요.",
                null,
                null,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createAccountVerificationFailedNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, memberId, occurredAt);
        saveNotification(
                eventId,
                traceId,
                memberId,
                NotificationType.ACCOUNT_VERIFICATION_FAILED,
                "계좌 인증에 실패했어요",
                "인증 시도 횟수를 초과했어요. 다시 시도해 주세요.",
                null,
                null,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createMemberOauthLinkedNotification(
            UUID eventId,
            String traceId,
            UUID memberId,
            String provider,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, memberId, occurredAt);
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider is required.");
        }

        saveNotification(
                eventId,
                traceId,
                memberId,
                NotificationType.MEMBER_OAUTH_LINKED,
                "소셜 계정 연동이 완료되었어요",
                provider + " 계정 연동이 완료되었어요.",
                null,
                null,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createOrderCreatedNotifications(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            Long totalAmount,
            List<UUID> sellerMemberIds,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, buyerMemberId, occurredAt);
        validateOrderArguments(orderId, totalAmount);

        Set<UUID> distinctSellerMemberIds = validateAndDistinctSellerMemberIds(sellerMemberIds);

        saveNotification(
                eventId,
                traceId,
                buyerMemberId,
                NotificationType.BUYER_ORDER_CREATED,
                "주문이 접수되었어요",
                "주문이 정상적으로 생성되었어요. 결제 금액: " + totalAmount + "원",
                orderId,
                NotificationReferenceType.ORDER,
                occurredAt
        );

        for (UUID sellerMemberId : distinctSellerMemberIds) {
            saveNotification(
                    eventId,
                    traceId,
                    sellerMemberId,
                    NotificationType.SELLER_ORDER_RECEIVED,
                    "새 주문이 접수되었어요",
                    "새 주문이 들어왔어요. 주문 내용을 확인하고 준비를 시작해 주세요.",
                    orderId,
                    NotificationReferenceType.ORDER,
                    occurredAt
            );
        }
    }

    @Override
    @Transactional
    public void createOrderCanceledNotifications(
            UUID eventId,
            String traceId,
            UUID orderId,
            UUID buyerMemberId,
            List<UUID> sellerMemberIds,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, buyerMemberId, occurredAt);
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required.");
        }

        Set<UUID> distinctSellerMemberIds = validateAndDistinctSellerMemberIds(sellerMemberIds);

        saveNotification(
                eventId,
                traceId,
                buyerMemberId,
                NotificationType.BUYER_ORDER_CANCELED,
                "주문 취소가 완료되었어요",
                "주문이 정상적으로 취소되었어요.",
                orderId,
                NotificationReferenceType.ORDER,
                occurredAt
        );

        for (UUID sellerMemberId : distinctSellerMemberIds) {
            saveNotification(
                    eventId,
                    traceId,
                    sellerMemberId,
                    NotificationType.SELLER_ORDER_CANCELED,
                    "주문 취소가 발생했어요",
                    "구매자가 주문을 취소했어요. 주문 상태를 확인해 주세요.",
                    orderId,
                    NotificationReferenceType.ORDER,
                    occurredAt
            );
        }
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
                NotificationType.BUYER_AUTO_PURCHASE_CONFIRMED,
                "자동 구매 확정이 완료되었어요",
                "주문이 자동으로 구매 확정되었어요.",
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
                NotificationType.BUYER_ORDER_PAYMENT_SUCCEEDED,
                "결제가 완료되었어요",
                "결제가 정상적으로 완료되었어요. 결제 금액: " + paidAmount + "원",
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
                NotificationType.BUYER_ORDER_PAYMENT_FAILED,
                "결제에 실패했어요",
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
                "정산 지급이 완료되었어요",
                "정산금 지급이 완료되었어요. 지급 금액: " + payoutAmount + "원",
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
                "정산 지급에 실패했어요",
                mapPayoutFailureReasonToContent(failureReason),
                settlementId,
                NotificationReferenceType.SETTLEMENT,
                processedAt
        );
    }

    @Override
    @Transactional
    public void createAuctionOutbidNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID outbidBidderId,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, outbidBidderId, occurredAt);
        if (auctionId == null) {
            throw new IllegalArgumentException("auctionId is required.");
        }

        saveNotification(
                eventId,
                traceId,
                outbidBidderId,
                NotificationType.BUYER_AUCTION_OUTBID,
                "최고 입찰가가 갱신되었어요",
                "참여 중인 경매에서 다른 입찰자가 더 높은 금액을 제시했어요.",
                auctionId,
                NotificationReferenceType.AUCTION,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createAuctionWonNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID winnerMemberId,
            String auctionTitle,
            Long finalPrice,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, winnerMemberId, occurredAt);
        validateAuctionArguments(auctionId, auctionTitle);
        validatePositiveAmount(finalPrice, "finalPrice");

        saveNotification(
                eventId,
                traceId,
                winnerMemberId,
                NotificationType.BUYER_AUCTION_WON,
                "경매에 낙찰되었어요",
                auctionTitle + " 경매가 " + finalPrice + "원에 낙찰되었어요.",
                auctionId,
                NotificationReferenceType.AUCTION,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createAuctionClosedSoldNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID sellerMemberId,
            String auctionTitle,
            Long finalPrice,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, sellerMemberId, occurredAt);
        validateAuctionArguments(auctionId, auctionTitle);
        validatePositiveAmount(finalPrice, "finalPrice");

        saveNotification(
                eventId,
                traceId,
                sellerMemberId,
                NotificationType.SELLER_AUCTION_CLOSED_SOLD,
                "경매가 낙찰로 종료되었어요",
                auctionTitle + " 경매가 " + finalPrice + "원에 낙찰되었어요.",
                auctionId,
                NotificationReferenceType.AUCTION,
                occurredAt
        );
    }

    @Override
    @Transactional
    public void createAuctionClosedUnsoldNotification(
            UUID eventId,
            String traceId,
            UUID auctionId,
            UUID sellerMemberId,
            String auctionTitle,
            LocalDateTime occurredAt
    ) {
        validateCommonArguments(eventId, sellerMemberId, occurredAt);
        validateAuctionArguments(auctionId, auctionTitle);

        saveNotification(
                eventId,
                traceId,
                sellerMemberId,
                NotificationType.SELLER_AUCTION_CLOSED_UNSOLD,
                "경매가 유찰되었어요",
                auctionTitle + " 경매가 입찰 없이 종료되었어요.",
                auctionId,
                NotificationReferenceType.AUCTION,
                occurredAt
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
        if (!type.supportsChannel(NotificationChannel.INBOX)) {
            throw new IllegalStateException("NotificationType must support INBOX to be persisted. type=" + type);
        }

        notificationMetricsRecorder.recordEventReceived(type);

        if (notificationJpaRepository.existsByEventIdAndMemberIdAndType(eventId, memberId, type)) {
            notificationMetricsRecorder.recordDuplicateEvent(type);
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
            notificationMetricsRecorder.recordSaved(type);

            if (type.supportsChannel(NotificationChannel.PUSH)) {
                pushAfterCommit(NotificationResponse.from(savedNotification));
            }
        } catch (RuntimeException e) {
            notificationMetricsRecorder.recordSaveFailed(type, NotificationMetricReason.DB_ERROR.name());
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
        validatePositiveAmount(paidAmount, "paidAmount");
    }

    private void validateAuctionArguments(UUID auctionId, String auctionTitle) {
        if (auctionId == null) {
            throw new IllegalArgumentException("auctionId is required.");
        }
        if (auctionTitle == null || auctionTitle.isBlank()) {
            throw new IllegalArgumentException("auctionTitle is required.");
        }
    }

    private void validatePositiveAmount(Long amount, String fieldName) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }

    private Set<UUID> validateAndDistinctSellerMemberIds(List<UUID> sellerMemberIds) {
        if (sellerMemberIds == null || sellerMemberIds.isEmpty()) {
            throw new IllegalArgumentException("sellerMemberIds is required.");
        }

        LinkedHashSet<UUID> distinctSellerMemberIds = new LinkedHashSet<>();
        for (UUID sellerMemberId : sellerMemberIds) {
            if (sellerMemberId == null) {
                throw new IllegalArgumentException("sellerMemberIds must not contain null.");
            }
            distinctSellerMemberIds.add(sellerMemberId);
        }

        return distinctSellerMemberIds;
    }

    private String mapFailureReasonToContent(OrderPaymentFailureReason failureReason) {
        return switch (failureReason) {
            case INSUFFICIENT_BALANCE -> "잔액이 부족해 결제에 실패했어요.";
            case WALLET_NOT_FOUND -> "지갑 정보를 찾을 수 없어 결제에 실패했어요.";
            case INVALID_REQUEST -> "요청 정보가 올바르지 않아 결제에 실패했어요.";
            case DUPLICATE_ORDER_PAYMENT -> "이미 처리된 주문 결제예요.";
            case INTERNAL_ERROR -> "일시적인 내부 오류로 결제에 실패했어요.";
        };
    }

    private String mapPayoutFailureReasonToContent(PayoutFailureReason failureReason) {
        return switch (failureReason) {
            case WALLET_NOT_FOUND -> "지갑 정보를 찾을 수 없어 정산 지급에 실패했어요.";
            case INVALID_PAYOUT_AMOUNT -> "지급 금액이 올바르지 않아 정산 지급에 실패했어요.";
            case DUPLICATE_PAYOUT -> "이미 처리된 정산 지급이에요.";
            case SETTLEMENT_NOT_FOUND -> "정산 정보를 찾을 수 없어 정산 지급에 실패했어요.";
            case TEMPORARY_DB_ERROR -> "일시적인 데이터베이스 오류로 정산 지급에 실패했어요.";
            case KAFKA_PUBLISH_ERROR -> "이벤트 발행 중 오류가 발생해 정산 지급에 실패했어요.";
            case INTERNAL_ERROR -> "일시적인 내부 오류로 정산 지급에 실패했어요.";
        };
    }
}
