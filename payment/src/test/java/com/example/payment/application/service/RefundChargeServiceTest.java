package com.example.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.dto.ChargeRefundResult;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.domain.exception.ChargeStateException;
import com.example.payment.domain.exception.PaymentGatewayException;
import com.example.payment.domain.repository.ChargeRefundRepository;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.domain.service.TossPaymentGateway;
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
@DisplayName("RefundChargeService 테스트")
class RefundChargeServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private ChargeRefundRepository chargeRefundRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private TossPaymentGateway tossPaymentGateway;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private RefundChargeService refundChargeService;

    private UUID memberId;
    private UUID walletId;
    private UUID chargeId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        chargeId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    @Nested
    @DisplayName("refundCharge() 충전 환불 테스트")
    class RefundCharge {

        private Charge successCharge;
        private Wallet wallet;

        @BeforeEach
        void setUp() {
            String pgOrderId = "CHARGE-" + chargeId;
            successCharge = Charge.create(
                    chargeId, memberId, walletId, 10_000L, PgProvider.TOSS, pgOrderId, now
            );
            successCharge.approve(10_000L, "paymentKey-001", now.plusMinutes(1));
            wallet = Wallet.create(walletId, memberId, 15_000L, now, now);
        }

        @Test
        @DisplayName("환불 성공 시 charge는 REFUNDED가 되고 wallet 잔액이 차감된다")
        void refundCharge_success_updatesChargeAndWallet() {
            ChargeRefundCommand command = new ChargeRefundCommand(chargeId, "user request");
            LocalDateTime refundedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentCancellation cancellation =
                    new TossPaymentGateway.TossPaymentCancellation("paymentKey-001", 10_000L, refundedAt);

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(successCharge));
            given(chargeRefundRepository.existsRefundedByChargeId(chargeId)).willReturn(false);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(tossPaymentGateway.cancel("paymentKey-001", "user request", 10_000L)).willReturn(cancellation);
            given(timeProvider.now()).willReturn(now.plusMinutes(4));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID(), UUID.randomUUID());
            given(chargeRefundRepository.save(any(ChargeRefund.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            ChargeRefundResult result = refundChargeService.refundCharge(command);

            assertThat(result.refundStatus()).isEqualTo(ChargeRefundStatus.REFUNDED);
            assertThat(result.walletBalance()).isEqualTo(5_000L);
            assertThat(result.refundedAmount()).isEqualTo(10_000L);
            verify(chargeRefundRepository).save(any(ChargeRefund.class));
            verify(walletTransactionRepository).save(any());
        }

        @Test
        @DisplayName("현재 잔액이 환불 금액보다 적으면 환불할 수 없다")
        void refundCharge_usedBalance_throwsException() {
            ChargeRefundCommand command = new ChargeRefundCommand(chargeId, "user request");
            wallet = Wallet.create(walletId, memberId, 9_000L, now, now);

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(successCharge));
            given(chargeRefundRepository.existsRefundedByChargeId(chargeId)).willReturn(false);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));

            assertThatThrownBy(() -> refundChargeService.refundCharge(command))
                    .isInstanceOf(ChargeStateException.class)
                    .hasMessageContaining("already been used");

            verify(tossPaymentGateway, never()).cancel(any(), any(), any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("토스 취소 실패 시 charge는 REFUND_FAILED로 저장된다")
        void refundCharge_gatewayFails_marksRefundFailed() {
            ChargeRefundCommand command = new ChargeRefundCommand(chargeId, "user request");

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(successCharge));
            given(chargeRefundRepository.existsRefundedByChargeId(chargeId)).willReturn(false);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(tossPaymentGateway.cancel("paymentKey-001", "user request", 10_000L))
                    .willThrow(new PaymentGatewayException("cancel rejected"));
            given(timeProvider.now()).willReturn(now.plusMinutes(4), now.plusMinutes(5));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(chargeRefundRepository.save(any(ChargeRefund.class))).willAnswer(invocation -> invocation.getArgument(0));

            assertThatThrownBy(() -> refundChargeService.refundCharge(command))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessageContaining("cancel rejected");

            assertThat(successCharge.getChargeStatus()).isEqualTo(ChargeStatus.SUCCESS);
            verify(chargeRefundRepository).save(any(ChargeRefund.class));
            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 환불 완료된 charge는 재환불할 수 없다")
        void refundCharge_alreadyRefunded_throwsException() {
            ChargeRefundCommand command = new ChargeRefundCommand(chargeId, "user request");

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(successCharge));
            given(chargeRefundRepository.existsRefundedByChargeId(chargeId)).willReturn(true);

            assertThatThrownBy(() -> refundChargeService.refundCharge(command))
                    .isInstanceOf(ChargeStateException.class)
                    .hasMessageContaining("already been completed");

            verify(tossPaymentGateway, never()).cancel(any(), any(), any());
        }
    }
}
