package com.example.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeConfirmResult;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.common.exception.PaymentGatewayException;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
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
    @DisplayName("confirmCharge")
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
        @DisplayName("success updates charge and wallet")
        void confirmCharge_success_updatesChargeAndWallet() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation(
                            "payKey-001",
                            pgOrderId,
                            10_000L,
                            approvedAt,
                            "계좌이체",
                            "92"
                    );

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            ChargeConfirmResult result = confirmChargeService.confirmCharge(command);

            assertThat(result.chargeStatus()).isEqualTo(ChargeStatus.CONFIRM_SUCCESS);
            assertThat(result.approvedAmount()).isEqualTo(10_000L);
            assertThat(result.walletBalance()).isEqualTo(15_000L);
            assertThat(result.chargeId()).isEqualTo(chargeId);
            assertThat(result.approvedAt()).isEqualTo(approvedAt);
            assertThat(pendingCharge.getTossBankCode()).isEqualTo("92");
            verify(chargeRepository).save(any(Charge.class));
            verify(walletRepository).save(any(Wallet.class));
            verify(walletTransactionRepository).save(any());
        }

        @Test
        @DisplayName("계좌이체가 아니면 tossBankCode는 저장하지 않는다")
        void confirmCharge_nonTransferMethod_doesNotStoreBankCode() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation(
                            "payKey-001",
                            pgOrderId,
                            10_000L,
                            approvedAt,
                            "카드",
                            null
                    );

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
            given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            ChargeConfirmResult result = confirmChargeService.confirmCharge(command);

            assertThat(result.chargeStatus()).isEqualTo(ChargeStatus.CONFIRM_SUCCESS);
            assertThat(pendingCharge.getTossBankCode()).isNull();
        }

        @Test
        @DisplayName("계좌이체인데 bankCode가 없으면 예외가 발생한다")
        void confirmCharge_transferWithoutBankCode_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation(
                            "payKey-001",
                            pgOrderId,
                            10_000L,
                            approvedAt,
                            "계좌이체",
                            null
                    );

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessageContaining("bankCode");

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("missing charge throws not found")
        void confirmCharge_chargeNotFound_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 10_000L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(ChargeNotFoundException.class);
        }

        @Test
        @DisplayName("non pending charge throws illegal state")
        void confirmCharge_alreadySuccessCharge_throwsIllegalStateException() {
            pendingCharge.approve(10_000L, "payKey-001", now.plusMinutes(1));
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-002", pgOrderId, 10_000L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Charge is not pending.");

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("pg order mismatch throws invalid request")
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
        @DisplayName("amount mismatch throws invalid request")
        void confirmCharge_amountMismatch_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, 99_999L);
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class)
                    .hasMessageContaining("amount does not match charge.");
        }

        @Test
        @DisplayName("gateway failure marks charge failed only")
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

            assertThat(pendingCharge.getChargeStatus()).isEqualTo(ChargeStatus.CONFIRM_FAILED);
            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("null charge id throws invalid request")
        void confirmCharge_nullChargeId_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(null, "payKey-001", pgOrderId, 10_000L);

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class)
                    .hasMessageContaining("chargeId is required.");
        }

        @Test
        @DisplayName("blank payment key throws invalid request")
        void confirmCharge_blankPaymentKey_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "  ", pgOrderId, 10_000L);

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class)
                    .hasMessageContaining("paymentKey is required.");
        }
    }
}
