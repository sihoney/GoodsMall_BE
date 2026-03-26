package com.example.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;
import com.example.payment.common.exception.EscrowNotFoundException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.enumtype.EscrowStatus;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.AutoPurchaseConfirmedEventPublisher;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.SellerIncomeReleasedEventPublisher;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscrowReleaseService 테스트")
class EscrowReleaseServiceTest {

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @Mock
    private AutoPurchaseConfirmedEventPublisher autoPurchaseConfirmedEventPublisher;

    @Mock
    private SellerIncomeReleasedEventPublisher sellerIncomeReleasedEventPublisher;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private EscrowReleaseService escrowReleaseService;

    private UUID orderId;
    private UUID buyerMemberId;
    private UUID sellerMemberId;
    private UUID sellerWalletId;
    private UUID escrowId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        buyerMemberId = UUID.randomUUID();
        sellerMemberId = UUID.randomUUID();
        sellerWalletId = UUID.randomUUID();
        escrowId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
    }

    @Nested
    @DisplayName("releaseEscrow() 테스트")
    class ReleaseEscrow {

        @Test
        @DisplayName("정상 해제 시 판매자 지갑이 증가하고 escrow가 RELEASED가 된다")
        void releaseEscrow_success_releasesEscrowAndIncreasesSellerBalance() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, 10_000L, now.plusDays(7), now.minusDays(1)
            );
            Wallet sellerWallet = Wallet.create(sellerWalletId, sellerMemberId, 5_000L, now, now.minusDays(2));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(walletRepository.findByMemberId(sellerMemberId)).willReturn(Optional.of(sellerWallet));
            given(timeProvider.now()).willReturn(now);
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(escrowRepository.save(any(Escrow.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            EscrowReleaseResult result = escrowReleaseService.releaseEscrow(command);

            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.sellerWalletId()).isEqualTo(sellerWalletId);
            assertThat(result.releasedAmount()).isEqualTo(10_000L);
            assertThat(result.sellerWalletBalance()).isEqualTo(15_000L);
            assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.RELEASED);
            verify(escrowRepository).save(any(Escrow.class));
            verify(walletRepository).save(any(Wallet.class));
            verify(walletTransactionRepository).save(any());
            verify(sellerIncomeReleasedEventPublisher).publish(any());
            verify(autoPurchaseConfirmedEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("자동 구매확정 해제 시 자동구매확정 이벤트도 발행한다")
        void releaseEscrow_autoConfirmation_publishesBuyerNotificationEvent() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.AUTO);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, 10_000L, now.plusDays(7), now.minusDays(1)
            );
            Wallet sellerWallet = Wallet.create(sellerWalletId, sellerMemberId, 5_000L, now, now.minusDays(2));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(walletRepository.findByMemberId(sellerMemberId)).willReturn(Optional.of(sellerWallet));
            given(timeProvider.now()).willReturn(now);
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(escrowRepository.save(any(Escrow.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            escrowReleaseService.releaseEscrow(command);

            verify(sellerIncomeReleasedEventPublisher).publish(any());
            verify(autoPurchaseConfirmedEventPublisher).publish(any());
        }

        @Test
        @DisplayName("escrow가 없으면 EscrowNotFoundException이 발생한다")
        void releaseEscrow_escrowNotFound_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(EscrowNotFoundException.class);
        }

        @Test
        @DisplayName("이미 RELEASED 상태면 기존 해제 결과를 재사용한다")
        void releaseEscrow_alreadyReleased_returnsExistingResult() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, 10_000L, now.plusDays(7), now.minusDays(1)
            );
            escrow.release(now.minusHours(1), now.minusHours(1));
            Wallet sellerWallet = Wallet.create(sellerWalletId, sellerMemberId, 15_000L, now, now.minusDays(2));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(walletRepository.findByMemberId(sellerMemberId)).willReturn(Optional.of(sellerWallet));

            EscrowReleaseResult result = escrowReleaseService.releaseEscrow(command);

            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.sellerWalletId()).isEqualTo(sellerWalletId);
            assertThat(result.releasedAmount()).isEqualTo(10_000L);
            assertThat(result.sellerWalletBalance()).isEqualTo(15_000L);
            assertThat(result.escrowStatus()).isEqualTo(EscrowStatus.RELEASED);
            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
            verify(sellerIncomeReleasedEventPublisher, never()).publish(any());
            verify(autoPurchaseConfirmedEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("이미 REFUNDED 상태면 IllegalStateException이 발생한다")
        void releaseEscrow_refunded_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, 10_000L, now.plusDays(7), now.minusDays(1)
            );
            escrow.refund(now.minusHours(1), now.minusHours(1));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not releasable");

            verify(sellerIncomeReleasedEventPublisher, never()).publish(any());
            verify(autoPurchaseConfirmedEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("판매자 지갑이 없으면 WalletNotFoundException이 발생한다")
        void releaseEscrow_sellerWalletNotFound_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId, ConfirmationType.MANUAL);
            Escrow escrow = Escrow.createHeld(
                    escrowId, orderId, buyerMemberId, sellerMemberId, 10_000L, now.plusDays(7), now.minusDays(1)
            );

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(walletRepository.findByMemberId(sellerMemberId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(WalletNotFoundException.class);

            verify(sellerIncomeReleasedEventPublisher, never()).publish(any());
            verify(autoPurchaseConfirmedEventPublisher, never()).publish(any());
        }
    }
}
