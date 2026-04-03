package com.example.notification.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerSettlementPayoutResultEventConsumerTest {

    @Mock
    private NotificationUsecase notificationUsecase;

    private SellerSettlementPayoutResultEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SellerSettlementPayoutResultEventConsumer(notificationUsecase);
    }

    @Test
    void listen_successDelegatesToSucceededNotification() {
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime processedAt = LocalDateTime.of(2026, 3, 29, 10, 20, 4);
        SellerSettlementPayoutResultMessage event = new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                sellerMemberId,
                180000L,
                SellerSettlementPayoutResultStatus.SUCCESS,
                null,
                processedAt
        );

        consumer.listen(event);

        verify(notificationUsecase).createSellerSettlementPayoutSucceededNotification(
                settlementId,
                sellerMemberId,
                180000L,
                processedAt
        );
    }

    @Test
    void listen_failureDelegatesToFailedNotification() {
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime processedAt = LocalDateTime.of(2026, 3, 29, 10, 20, 4);
        SellerSettlementPayoutResultMessage event = new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                sellerMemberId,
                180000L,
                SellerSettlementPayoutResultStatus.FAILED,
                PayoutFailureReason.WALLET_NOT_FOUND,
                processedAt
        );

        consumer.listen(event);

        verify(notificationUsecase).createSellerSettlementPayoutFailedNotification(
                settlementId,
                sellerMemberId,
                PayoutFailureReason.WALLET_NOT_FOUND,
                processedAt
        );
    }

    @Test
    void listen_throwsWhenFailureReasonMissingOnFailure() {
        SellerSettlementPayoutResultMessage event = new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                180000L,
                SellerSettlementPayoutResultStatus.FAILED,
                null,
                LocalDateTime.of(2026, 3, 29, 10, 20, 4)
        );

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("failureReason is required for failure.");
    }
}
