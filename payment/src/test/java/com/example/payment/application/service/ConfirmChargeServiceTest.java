package com.example.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeConfirmResult;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.domain.exception.ChargeNotFoundException;
import com.example.payment.domain.exception.ChargeStateException;
import com.example.payment.domain.exception.InvalidChargeRequestException;
import com.example.payment.domain.exception.PaymentGatewayException;
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
@DisplayName("ConfirmChargeService 테스트")
class ConfirmChargeServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

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
    private ConfirmChargeService confirmChargeService;

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
    @DisplayName("confirmCharge() 충전 승인 테스트")
    class ConfirmCharge {

        private Charge pendingCharge;
        private Wallet wallet;
        private String pgOrderId;

        @BeforeEach
        void setUp() {
            pgOrderId = "CHARGE-" + chargeId;
            pendingCharge = Charge.create(
                    chargeId, memberId, walletId, 10_000L, PgProvider.TOSS, pgOrderId, now
            );
            wallet = Wallet.create(walletId, memberId, 5_000L, now, now);
        }

        @Test
        @DisplayName("승인 성공 시 charge 상태가 SUCCESS로 변경된다")
        void confirmCharge_success_chargeStatusIsSuccess() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation("payKey-001", pgOrderId, 10_000L, approvedAt);

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            ChargeConfirmResult result = confirmChargeService.confirmCharge(command);

            assertThat(result.chargeStatus()).isEqualTo(ChargeStatus.SUCCESS);
            assertThat(result.approvedAmount()).isEqualTo(10_000L);
        }

        @Test
        @DisplayName("승인 성공 시 wallet 잔액이 증가한다")
        void confirmCharge_success_walletBalanceIncreases() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation("payKey-001", pgOrderId, 10_000L, approvedAt);

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            ChargeConfirmResult result = confirmChargeService.confirmCharge(command);

            assertThat(result.walletBalance()).isEqualTo(15_000L);
        }

        @Test
        @DisplayName("승인 성공 시 charge, wallet, walletTransaction이 모두 저장된다")
        void confirmCharge_success_savesAllThreeEntities() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation("payKey-001", pgOrderId, 10_000L, approvedAt);

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            confirmChargeService.confirmCharge(command);

            verify(chargeRepository).save(any(Charge.class));
            verify(walletRepository).save(any(Wallet.class));
            verify(walletTransactionRepository).save(any());
        }

        @Test
        @DisplayName("charge를 찾지 못하면 ChargeNotFoundException이 발생한다")
        void confirmCharge_chargeNotFound_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(ChargeNotFoundException.class);
        }

        @Test
        @DisplayName("이미 SUCCESS인 charge를 재승인하면 ChargeStateException이 발생한다")
        void confirmCharge_alreadySuccessCharge_throwsChargeStateException() {
            pendingCharge.approve(10_000L, "payKey-001", now.plusMinutes(1));
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-002", pgOrderId, 10_000L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(ChargeStateException.class)
                    .hasMessageContaining("Charge is not pending.");

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("pgOrderId가 불일치하면 InvalidChargeRequestException이 발생한다")
        void confirmCharge_pgOrderIdMismatch_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(
                    chargeId, "payKey-001", "WRONG-ORDER-ID", 10_000L
            );
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class)
                    .hasMessageContaining("pgOrderId does not match charge.");
        }

        @Test
        @DisplayName("amount가 불일치하면 InvalidChargeRequestException이 발생한다")
        void confirmCharge_amountMismatch_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 99_999L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class)
                    .hasMessageContaining("amount does not match charge.");
        }

        @Test
        @DisplayName("토스 게이트웨이 호출 실패 시 wallet이 저장되지 않는다")
        void confirmCharge_tossGatewayFails_walletNotSaved() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any()))
                    .willThrow(new PaymentGatewayException("Toss gateway error"));
            given(timeProvider.now()).willReturn(now.plusMinutes(2));
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(PaymentGatewayException.class);

            verify(walletRepository, never()).findByWalletId(any());
            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
            verify(chargeRepository).save(pendingCharge);
            assertThat(pendingCharge.getChargeStatus()).isEqualTo(ChargeStatus.FAILED);
            assertThat(pendingCharge.getFailureReason()).contains("Toss gateway error");
            assertThat(pendingCharge.getFailedAt()).isEqualTo(now.plusMinutes(2));
        }

        @Test
        @DisplayName("토스 게이트웨이 호출 실패 시 charge만 FAILED로 저장된다")
        void confirmCharge_tossGatewayFails_marksChargeFailedOnly() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any()))
                    .willThrow(new PaymentGatewayException("confirm rejected"));
            given(timeProvider.now()).willReturn(now.plusMinutes(3));
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessageContaining("confirm rejected");

            assertThat(pendingCharge.getChargeStatus()).isEqualTo(ChargeStatus.FAILED);
            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("confirmCharge 요청 시 chargeId가 null이면 InvalidChargeRequestException이 발생한다")
        void confirmCharge_nullChargeId_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(null, "payKey-001", pgOrderId, 10_000L);

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class)
                    .hasMessageContaining("chargeId is required.");
        }

        @Test
        @DisplayName("confirmCharge 요청 시 paymentKey가 빈 문자열이면 InvalidChargeRequestException이 발생한다")
        void confirmCharge_blankPaymentKey_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "  ", pgOrderId, 10_000L);

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class)
                    .hasMessageContaining("paymentKey is required.");
        }

        @Test
        @DisplayName("승인 성공 시 반환된 결과에 chargeId와 approvedAt이 올바르게 포함된다")
        void confirmCharge_success_resultContainsCorrectFields() {
            LocalDateTime approvedAt = now.plusMinutes(5);
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation("payKey-001", pgOrderId, 10_000L, approvedAt);

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            ChargeConfirmResult result = confirmChargeService.confirmCharge(command);

            assertThat(result.chargeId()).isEqualTo(chargeId);
            assertThat(result.approvedAt()).isEqualTo(approvedAt);
        }
    }
}
