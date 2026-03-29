package com.example.payment.infrastructure.messaging.kafka;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.enumtype.WalletTransactionType;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerSettlementPayoutRequestedEventConsumer 테스트")
class SellerSettlementPayoutRequestedEventConsumerTest {
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private IdentifierGenerator identifierGenerator;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private KafkaSellerSettlementPayoutResultEventPublisher payoutResultEventPublisher;
    @InjectMocks
    private SellerSettlementPayoutRequestedEventConsumer consumer;
    @Test
    @DisplayName("지급 요청을 처리하면 seller wallet 적립 후 SUCCESS 결과를 발행한다")
    void listen_validEvent_creditsWalletAndPublishesSuccess() {
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 4, 1, 3, 10);
        SellerSettlementPayoutRequestedMessage event = new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                settlementId,
                sellerMemberId,
                2026,
                3,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        Wallet wallet = Wallet.create(walletId, sellerMemberId, 1_000L, now, now.minusDays(1));
        when(timeProvider.now()).thenReturn(now);
        when(identifierGenerator.generateUuid()).thenReturn(UUID.randomUUID(), UUID.randomUUID());
        when(walletTransactionRepository.findByReferenceIdAndReferenceType(settlementId, "SETTLEMENT"))
                .thenReturn(Optional.empty());
        when(walletRepository.findByMemberId(sellerMemberId)).thenReturn(Optional.of(wallet));
        consumer.listen(event);
        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
        ArgumentCaptor<SellerSettlementPayoutResultMessage> captor =
                ArgumentCaptor.forClass(SellerSettlementPayoutResultMessage.class);
        verify(payoutResultEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().resultStatus()).isEqualTo(SellerSettlementPayoutResultStatus.SUCCESS);
        assertThat(captor.getValue().failureReason()).isNull();
        assertThat(captor.getValue().settlementId()).isEqualTo(settlementId);
    }
    @Test
    @DisplayName("이미 같은 settlement 거래가 있으면 중복 지급 없이 SUCCESS 결과만 발행한다")
    void listen_duplicateEvent_publishesSuccessWithoutWalletMutation() {
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 4, 1, 3, 10);
        SellerSettlementPayoutRequestedMessage event = new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                settlementId,
                sellerMemberId,
                2026,
                3,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        WalletTransaction existingTransaction = WalletTransaction.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                9_000L,
                10_000L,
                WalletTransactionType.SETTLEMENT,
                settlementId,
                "SETTLEMENT",
                "seller settlement payout",
                now
        );
        when(timeProvider.now()).thenReturn(now);
        when(identifierGenerator.generateUuid()).thenReturn(UUID.randomUUID());
        when(walletTransactionRepository.findByReferenceIdAndReferenceType(settlementId, "SETTLEMENT"))
                .thenReturn(Optional.of(existingTransaction));
        consumer.listen(event);
        verify(walletRepository, never()).save(any());
        verify(walletTransactionRepository, never()).save(any());
        verify(payoutResultEventPublisher).publish(any());
    }
    @Test
    @DisplayName("seller wallet이 없으면 WALLET_NOT_FOUND 실패 사유로 FAILED 결과를 발행한다")
    void listen_walletNotFound_publishesFailedWithWalletNotFoundReason() {
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 4, 1, 3, 10);
        SellerSettlementPayoutRequestedMessage event = new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                settlementId,
                sellerMemberId,
                2026,
                3,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        when(timeProvider.now()).thenReturn(now);
        when(identifierGenerator.generateUuid()).thenReturn(UUID.randomUUID());
        when(walletTransactionRepository.findByReferenceIdAndReferenceType(settlementId, "SETTLEMENT"))
                .thenReturn(Optional.empty());
        when(walletRepository.findByMemberId(sellerMemberId)).thenReturn(Optional.empty());
        consumer.listen(event);
        verify(walletRepository, never()).save(any());
        ArgumentCaptor<SellerSettlementPayoutResultMessage> captor =
                ArgumentCaptor.forClass(SellerSettlementPayoutResultMessage.class);
        verify(payoutResultEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().resultStatus()).isEqualTo(SellerSettlementPayoutResultStatus.FAILED);
        assertThat(captor.getValue().failureReason()).isEqualTo(PayoutFailureReason.WALLET_NOT_FOUND);
        assertThat(captor.getValue().settlementId()).isEqualTo(settlementId);
    }
    @Test
    @DisplayName("필수 필드가 없으면 예외가 발생한다")
    void listen_missingSettlementId_throwsException() {
        SellerSettlementPayoutRequestedMessage event = new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                2026,
                3,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("settlementId");
    }
    @Test
    @DisplayName("지급 처리 중 RuntimeException이 발생하면 Kafka 재시도를 위해 예외를 전파한다")
    void listen_runtimeException_propagatesExceptionForKafkaRetry() {
        UUID settlementId = UUID.randomUUID();
        UUID sellerMemberId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 4, 1, 3, 10);
        SellerSettlementPayoutRequestedMessage event = new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                settlementId,
                sellerMemberId,
                2026,
                3,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        Wallet wallet = Wallet.create(UUID.randomUUID(), sellerMemberId, 1_000L, now, now.minusDays(1));

        when(timeProvider.now()).thenReturn(now);
        when(walletTransactionRepository.findByReferenceIdAndReferenceType(settlementId, "SETTLEMENT"))
                .thenReturn(Optional.empty());
        when(walletRepository.findByMemberId(sellerMemberId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenThrow(new RuntimeException("temporary db error"));

        assertThatThrownBy(() -> consumer.listen(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("temporary db error");

        verify(payoutResultEventPublisher, never()).publish(any());
    }
}