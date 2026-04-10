package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.PgProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Charge 도메인 테스트")
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

    private Charge createPendingCharge() {
        return Charge.create(
                chargeId,
                memberId,
                walletId,
                10_000L,
                PgProvider.TOSS,
                "CHARGE-" + chargeId,
                requestedAt
        );
    }

    @Nested
    @DisplayName("Charge.create() 생성 테스트")
    class Create {

        @Test
        @DisplayName("충전 생성 시 PENDING 상태로 시작한다")
        void createCharge_statusIsPending() {
            Charge charge = createPendingCharge();

            assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.PENDING);
        }

        @Test
        @DisplayName("충전 생성 시 요청 금액이 올바르게 저장된다")
        void createCharge_requestedAmountIsSet() {
            Charge charge = createPendingCharge();

            assertThat(charge.getRequestedAmount()).isEqualTo(10_000L);
        }

        @Test
        @DisplayName("충전 생성 시 승인 금액과 paymentKey는 null이다")
        void createCharge_approvedAmountAndPaymentKeyAreNull() {
            Charge charge = createPendingCharge();

            assertThat(charge.getApprovedAmount()).isNull();
            assertThat(charge.getPgPaymentKey()).isNull();
        }

        @Test
        @DisplayName("충전 생성 시 isPending()은 true다")
        void createCharge_isPendingIsTrue() {
            Charge charge = createPendingCharge();

            assertThat(charge.isPending()).isTrue();
        }
    }

    @Nested
    @DisplayName("Charge.approve() 승인 테스트")
    class Approve {

        @Test
        @DisplayName("PENDING 상태에서 approve() 호출 시 SUCCESS로 전이된다")
        void approve_pendingToSuccess() {
            Charge charge = createPendingCharge();
            LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.approve(10_000L, "paymentKey-abc", approvedAt);

            assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.CONFIRM_SUCCESS);
        }

        @Test
        @DisplayName("approve() 성공 시 승인 금액과 paymentKey가 저장된다")
        void approve_storesApprovedAmountAndPaymentKey() {
            Charge charge = createPendingCharge();
            LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.approve(10_000L, "paymentKey-abc", approvedAt);

            assertThat(charge.getApprovedAmount()).isEqualTo(10_000L);
            assertThat(charge.getPgPaymentKey()).isEqualTo("paymentKey-abc");
            assertThat(charge.getApprovedAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("approve() 성공 시 isPending()은 false다")
        void approve_isPendingIsFalse() {
            Charge charge = createPendingCharge();

            charge.approve(10_000L, "paymentKey-abc", LocalDateTime.now());

            assertThat(charge.isPending()).isFalse();
        }

        @Test
        @DisplayName("이미 SUCCESS인 Charge에 approve()를 호출하면 예외가 발생한다")
        void approve_alreadySuccess_throwsException() {
            Charge charge = createPendingCharge();
            charge.approve(10_000L, "paymentKey-abc", LocalDateTime.now());

            assertThatThrownBy(() -> charge.approve(10_000L, "paymentKey-abc2", LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending charges can be changed.");
        }

        @Test
        @DisplayName("FAILED 상태에서 approve()를 호출하면 예외가 발생한다")
        void approve_failedCharge_throwsException() {
            Charge charge = createPendingCharge();
            charge.fail("reason", LocalDateTime.now());

            assertThatThrownBy(() -> charge.approve(10_000L, "paymentKey-abc", LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending charges can be changed.");
        }
    }

    @Nested
    @DisplayName("Charge.fail() 실패 테스트")
    class Fail {

        @Test
        @DisplayName("PENDING 상태에서 fail() 호출 시 FAILED로 전이된다")
        void fail_pendingToFailed() {
            Charge charge = createPendingCharge();
            LocalDateTime failedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.fail("payment declined", failedAt);

            assertThat(charge.getChargeStatus()).isEqualTo(ChargeStatus.CONFIRM_FAILED);
        }

        @Test
        @DisplayName("fail() 호출 시 실패 사유와 시각이 저장된다")
        void fail_storesReasonAndTimestamp() {
            Charge charge = createPendingCharge();
            LocalDateTime failedAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);

            charge.fail("payment declined", failedAt);

            assertThat(charge.getFailureReason()).isEqualTo("payment declined");
            assertThat(charge.getFailedAt()).isEqualTo(failedAt);
        }

        @Test
        @DisplayName("이미 SUCCESS인 Charge에 fail()을 호출하면 예외가 발생한다")
        void fail_alreadySuccess_throwsException() {
            Charge charge = createPendingCharge();
            charge.approve(10_000L, "paymentKey-abc", LocalDateTime.now());

            assertThatThrownBy(() -> charge.fail("reason", LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending charges can be changed.");
        }

        @Test
        @DisplayName("이미 FAILED인 Charge에 fail()을 호출하면 예외가 발생한다")
        void fail_alreadyFailed_throwsException() {
            Charge charge = createPendingCharge();
            charge.fail("first reason", LocalDateTime.now());

            assertThatThrownBy(() -> charge.fail("second reason", LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending charges can be changed.");
        }
    }

}

