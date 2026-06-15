package com.example.payment.wallet.domain.entity;

import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("payment test")
class ChargeTest {

    private UUID chargeId;
    private UUID memberId;
    private UUID walletId;
    private LocalDateTime requestedAt;

    @BeforeEach
    void setUp() {
        chargeId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        requestedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    private Charge createPendingCharge() {
        return Charge.create(
                chargeId,
                memberId,
                walletId,
                amount(10_000L),
                "CHARGE-" + chargeId,
                requestedAt
        );
    }

    @Nested
    @DisplayName("payment test")
    class Create {

        @Test
        @DisplayName("payment test")
        void createCharge_statusIsPending() {
            Charge charge = createPendingCharge();

            assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.PENDING);
        }

        @Test
        @DisplayName("payment test")
        void createCharge_requestedAmountIsSet() {
            Charge charge = createPendingCharge();

            assertThat(charge.getRequestedAmount()).isEqualTo(amount(10_000L));
        }

        @Test
        @DisplayName("payment test")
        void createCharge_approvedAmountAndPaymentKeyAreNull() {
            Charge charge = createPendingCharge();

            assertThat(charge.getApprovedAmount()).isNull();
            assertThat(charge.getPgPaymentKey()).isNull();
        }

        @Test
        @DisplayName("payment test")
        void createCharge_isPendingIsTrue() {
            Charge charge = createPendingCharge();

            assertThat(charge.isPending()).isTrue();
        }
    }

    @Nested
    @DisplayName("payment test")
    class Approve {

        @Test
        @DisplayName("payment test")
        void approve_pendingToSuccess() {
            Charge charge = createPendingCharge();
            LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.approve(amount(10_000L), "paymentKey-abc", approvedAt, null);

            assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.CONFIRM_SUCCESS);
        }

        @Test
        @DisplayName("payment test")
        void approve_storesApprovedAmountAndPaymentKey() {
            Charge charge = createPendingCharge();
            LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.approve(amount(10_000L), "paymentKey-abc", approvedAt, null);

            assertThat(charge.getApprovedAmount()).isEqualTo(amount(10_000L));
            assertThat(charge.getPgPaymentKey()).isEqualTo("paymentKey-abc");
            assertThat(charge.getApprovedAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("payment test")
        void approve_isPendingIsFalse() {
            Charge charge = createPendingCharge();

            charge.approve(amount(10_000L), "paymentKey-abc", LocalDateTime.now(), null);

            assertThat(charge.isPending()).isFalse();
        }

        @Test
        @DisplayName("payment test")
        void approve_alreadySuccess_throwsException() {
            Charge charge = createPendingCharge();
            charge.approve(amount(10_000L), "paymentKey-abc", LocalDateTime.now(), null);

            assertThatThrownBy(() -> charge.approve(amount(10_000L), "paymentKey-abc2", LocalDateTime.now(), null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("payment test")
        void approve_failedCharge_throwsException() {
            Charge charge = createPendingCharge();
            charge.fail("reason", LocalDateTime.now());

            assertThatThrownBy(() -> charge.approve(amount(10_000L), "paymentKey-abc", LocalDateTime.now(), null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("payment test")
    class Fail {

        @Test
        @DisplayName("payment test")
        void fail_pendingToFailed() {
            Charge charge = createPendingCharge();
            LocalDateTime failedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.fail("payment declined", failedAt);

            assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.CONFIRM_FAILED);
        }

        @Test
        @DisplayName("payment test")
        void fail_storesReasonAndTimestamp() {
            Charge charge = createPendingCharge();
            LocalDateTime failedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.fail("payment declined", failedAt);

            assertThat(charge.getFailureReason()).isEqualTo("payment declined");
            assertThat(charge.getFailedAt()).isEqualTo(failedAt);
        }

        @Test
        @DisplayName("payment test")
        void fail_alreadySuccess_throwsException() {
            Charge charge = createPendingCharge();
            charge.approve(amount(10_000L), "paymentKey-abc", LocalDateTime.now(), null);

            assertThatThrownBy(() -> charge.fail("reason", LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("payment test")
        void fail_alreadyFailed_throwsException() {
            Charge charge = createPendingCharge();
            charge.fail("first reason", LocalDateTime.now());

            assertThatThrownBy(() -> charge.fail("second reason", LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

}

