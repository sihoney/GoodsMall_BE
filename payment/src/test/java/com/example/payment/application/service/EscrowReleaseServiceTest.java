package com.example.payment.application.service;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.enumtype.EscrowStatus;
import com.example.payment.domain.exception.EscrowAlreadyRefundedException;
import com.example.payment.domain.exception.EscrowAlreadyReleasedException;
import com.example.payment.domain.exception.EscrowNotFoundException;
import com.example.payment.domain.exception.EscrowStateException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.SellerIncomeReleasedEventPublisher;
import com.example.payment.domain.service.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscrowReleaseService 애플리케이션 서비스 테스트")
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
    private SellerIncomeReleasedEventPublisher sellerIncomeReleasedEventPublisher;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private EscrowReleaseService escrowReleaseService;

    private UUID orderId;
    private UUID sellerMemberId;
    private UUID sellerWalletId;
    private UUID escrowId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        sellerMemberId = UUID.randomUUID();
        sellerWalletId = UUID.randomUUID();
        escrowId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
    }

    @Nested
    @DisplayName("releaseEscrow() 에스크로 해제 테스트")
    class ReleaseEscrow {

        @Test
        @DisplayName("정상 해제 시 판매자 지갑이 증가하고 escrow가 RELEASED가 된다")
        void releaseEscrow_success_releasesEscrowAndIncreasesSellerBalance() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId);
            Escrow escrow = Escrow.createHeld(escrowId, orderId, 10_000L, now.plusDays(7), now.minusDays(1));
            Wallet sellerWallet = Wallet.create(sellerWalletId, sellerMemberId, 5_000L, now, now.minusDays(2));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(walletRepository.findByMemberId(sellerMemberId)).willReturn(Optional.of(sellerWallet));
            given(timeProvider.now()).willReturn(now);
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(escrowRepository.save(any(Escrow.class))).willAnswer(inv -> inv.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

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
        }

        @Test
        @DisplayName("에스크로가 없으면 EscrowNotFoundException이 발생한다")
        void releaseEscrow_escrowNotFound_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId);

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(EscrowNotFoundException.class);
        }

        @Test
        @DisplayName("이미 RELEASED 상태면 다시 해제할 수 없다")
        void releaseEscrow_alreadyReleased_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId);
            Escrow escrow = Escrow.createHeld(escrowId, orderId, 10_000L, now.plusDays(7), now.minusDays(1));
            escrow.release(now.minusHours(1), now.minusHours(1));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(EscrowAlreadyReleasedException.class)
                    .hasMessageContaining("already been released");

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
            verify(sellerIncomeReleasedEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("이미 REFUNDED 상태면 해제할 수 없다")
        void releaseEscrow_refunded_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId);
            Escrow escrow = Escrow.createHeld(escrowId, orderId, 10_000L, now.plusDays(7), now.minusDays(1));
            escrow.refund(now.minusHours(1), now.minusHours(1));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(EscrowAlreadyRefundedException.class)
                    .hasMessageContaining("already been refunded");

            verify(sellerIncomeReleasedEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("판매자 지갑이 없으면 WalletNotFoundException이 발생한다")
        void releaseEscrow_sellerWalletNotFound_throwsException() {
            EscrowReleaseCommand command = new EscrowReleaseCommand(orderId, sellerMemberId);
            Escrow escrow = Escrow.createHeld(escrowId, orderId, 10_000L, now.plusDays(7), now.minusDays(1));

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(escrow));
            given(walletRepository.findByMemberId(sellerMemberId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> escrowReleaseService.releaseEscrow(command))
                    .isInstanceOf(WalletNotFoundException.class);

            verify(sellerIncomeReleasedEventPublisher, never()).publish(any());
        }
    }
}
