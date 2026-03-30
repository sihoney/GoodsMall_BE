package com.example.notification.infrastructure.messaging.kafka;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SellerSettlementPayoutResultEventConsumer {

    private final NotificationUsecase notificationUsecase;

    public SellerSettlementPayoutResultEventConsumer(NotificationUsecase notificationUsecase) {
        this.notificationUsecase = notificationUsecase;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.seller-settlement-payout-result:payment.seller-payout-result}",
            groupId = "${notification.kafka.consumer-groups.seller-settlement-payout-result:notification-service}",
            containerFactory = "sellerSettlementPayoutResultKafkaListenerContainerFactory"
    )
    public void listen(SellerSettlementPayoutResultMessage event) {
        validateEvent(event);

        if (event.resultStatus() == SellerSettlementPayoutResultStatus.SUCCESS) {
            notificationUsecase.createSellerSettlementPayoutSucceededNotification(
                    event.settlementId(),
                    event.sellerMemberId(),
                    event.payoutAmount(),
                    event.processedAt()
            );
            return;
        }

        notificationUsecase.createSellerSettlementPayoutFailedNotification(
                event.settlementId(),
                event.sellerMemberId(),
                event.failureReason(),
                event.processedAt()
        );
    }

    private void validateEvent(SellerSettlementPayoutResultMessage event) {
        if (event == null) {
            throw new IllegalArgumentException("sellerSettlementPayoutResult event is required.");
        }
        if (event.settlementId() == null) {
            throw new IllegalArgumentException("settlementId is required.");
        }
        if (event.sellerMemberId() == null) {
            throw new IllegalArgumentException("sellerMemberId is required.");
        }
        if (event.resultStatus() == null) {
            throw new IllegalArgumentException("resultStatus is required.");
        }
        if (event.processedAt() == null) {
            throw new IllegalArgumentException("processedAt is required.");
        }
        if (event.payoutAmount() == null || event.payoutAmount() <= 0) {
            throw new IllegalArgumentException("payoutAmount must be positive.");
        }
        if (event.resultStatus() == SellerSettlementPayoutResultStatus.FAILED && event.failureReason() == null) {
            throw new IllegalArgumentException("failureReason is required for failure.");
        }
    }
}
