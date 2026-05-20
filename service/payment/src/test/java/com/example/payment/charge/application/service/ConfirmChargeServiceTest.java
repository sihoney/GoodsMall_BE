package com.example.payment.charge.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.charge.application.dto.ChargeConfirmCommand;
import com.example.payment.charge.application.dto.ChargeConfirmResult;
import com.example.payment.common.common.exception.ChargeNotFoundException;
import com.example.payment.common.common.exception.InvalidChargeRequestException;
import com.example.payment.common.common.exception.PaymentGatewayException;
import com.example.payment.charge.domain.entity.Charge;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.charge.domain.enumtype.ChargeStatus;
import com.example.payment.charge.domain.repository.ChargeRepository;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import com.example.payment.card.domain.service.TossPaymentGateway;
import java.math.BigDecimal;
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
@DisplayName("payment test")
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

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    @Nested
    @DisplayName("payment test")
    class ConfirmCharge {

        private Charge pendingCharge;
        private Wallet wallet;
        private String pgOrderId;

        @BeforeEach
        void setUp() {
            pgOrderId = "CHARGE-" + chargeId;
            pendingCharge = Charge.create(
                    chargeId, memberId, walletId, amount(10_000L), pgOrderId, now
            );
            wallet = Wallet.create(walletId, memberId, amount(5_000L), now, now);
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_success_updatesChargeAndWallet() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, amount(10_000L));
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation(
                            "payKey-001",
                            pgOrderId,
                            amount(10_000L),
                            approvedAt,
                            "怨꾩쥖?댁껜",
                            "92",
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
            assertThat(result.approvedAmount()).isEqualTo(amount(10_000L));
            assertThat(result.walletBalance()).isEqualTo(amount(15_000L));
            assertThat(result.chargeId()).isEqualTo(chargeId);
            assertThat(result.approvedAt()).isEqualTo(approvedAt);
            assertThat(pendingCharge.getTossBankCode()).isEqualTo("92");
            verify(chargeRepository).save(any(Charge.class));
            verify(walletRepository).save(any(Wallet.class));
            verify(walletTransactionRepository).save(any());
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_nonTransferMethod_doesNotStoreBankCode() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, amount(10_000L));
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation(
                            "payKey-001",
                            pgOrderId,
                            amount(10_000L),
                            approvedAt,
                            "移대뱶",
                            null,
                            "?꾨?移대뱶"
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
        @DisplayName("payment test")
        void confirmCharge_transferWithoutBankCode_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, amount(10_000L));
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation(
                            "payKey-001",
                            pgOrderId,
                            amount(10_000L),
                            approvedAt,
                            "怨꾩쥖?댁껜",
                            null,
                            null
                    );

            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));
            given(tossPaymentGateway.confirm(any(), any(), any())).willReturn(confirmation);
            given(walletRepository.findByWalletId(walletId)).willReturn(Optional.of(wallet));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(PaymentGatewayException.class);

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_chargeNotFound_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, amount(10_000L));
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(ChargeNotFoundException.class);
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_alreadySuccessCharge_throwsIllegalStateException() {
            pendingCharge.approve(amount(10_000L), "payKey-001", now.plusMinutes(1), null);
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-002", pgOrderId, amount(10_000L));
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(IllegalStateException.class);

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_pgOrderIdMismatch_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(
                    chargeId, "payKey-001", "WRONG-ORDER-ID", amount(10_000L)
            );
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class);
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_amountMismatch_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, amount(99_999L));
            given(chargeRepository.findByChargeId(chargeId)).willReturn(Optional.of(pendingCharge));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class);
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_tossGatewayFails_marksChargeFailedOnly() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, amount(10_000L));
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
        @DisplayName("payment test")
        void confirmCharge_nullChargeId_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(null, "payKey-001", pgOrderId, amount(10_000L));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class);
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_blankPaymentKey_throwsException() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "  ", pgOrderId, amount(10_000L));

            assertThatThrownBy(() -> confirmChargeService.confirmCharge(command))
                    .isInstanceOf(InvalidChargeRequestException.class);
        }

        @Test
        @DisplayName("payment test")
        void confirmCharge_sameAmountDifferentScale_success() {
            ChargeConfirmCommand command = new ChargeConfirmCommand(chargeId, "payKey-001", pgOrderId, decimal("10000.00"));
            LocalDateTime approvedAt = now.plusMinutes(5);
            TossPaymentGateway.TossPaymentConfirmation confirmation =
                    new TossPaymentGateway.TossPaymentConfirmation(
                            "payKey-001",
                            pgOrderId,
                            decimal("10000.00"),
                            approvedAt,
                            "怨꾩쥖?댁껜",
                            "92",
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
            assertThat(result.approvedAmount()).isEqualByComparingTo(decimal("10000.00"));
            assertThat(result.walletBalance()).isEqualByComparingTo(decimal("15000.00"));
        }
    }
}
