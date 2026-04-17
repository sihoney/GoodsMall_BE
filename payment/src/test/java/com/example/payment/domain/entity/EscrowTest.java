package com.example.payment.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Escrow 테스트")
class EscrowTest {

    private final UUID escrowId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerMemberId = UUID.randomUUID();
    private final UUID sellerMemberId = UUID.randomUUID();
    private final LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

    @Nested
    @DisplayName("createHeld()")
    class CreateHeld {

        @Test
        @DisplayName("정상 생성 시 HELD 상태와 초기 금액을 설정한다")
        void createHeld_success() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    createdAt
            );

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.HELD);
            assertThat(escrow.getAmount()).isEqualTo(12_000L);
            assertThat(escrow.getOriginalAmount()).isEqualTo(12_000L);
            assertThat(escrow.getRefundedAmount()).isZero();
        }

        @Test
        @DisplayName("금액이 0 이하면 예외를 던진다")
        void createHeld_nonPositiveAmount_throwsException() {
            assertThatThrownBy(() -> Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    0L,
                    createdAt
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("release()")
    class Release {

        @Test
        @DisplayName("HELD 상태에서 RELEASED로 전이한다")
        void release_success() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    createdAt
            );
            LocalDateTime now = createdAt.plusDays(1);

            escrow.release(now, now);

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.RELEASED);
            assertThat(escrow.getReleasedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("applyRefundAmount()")
    class ApplyRefundAmount {

        @Test
        @DisplayName("부분 환불 시 amount/refundedAmount를 갱신한다")
        void applyRefundAmount_partial() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    createdAt
            );
            LocalDateTime now = createdAt.plusDays(1);

            escrow.applyRefundAmount(5_000L, now, now);

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.HELD);
            assertThat(escrow.getAmount()).isEqualTo(7_000L);
            assertThat(escrow.getRefundedAmount()).isEqualTo(5_000L);
        }

        @Test
        @DisplayName("전액 환불 시 REFUNDED 상태로 전이한다")
        void applyRefundAmount_full() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    createdAt
            );
            LocalDateTime now = createdAt.plusDays(1);

            escrow.applyRefundAmount(12_000L, now, now);

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.REFUNDED);
            assertThat(escrow.getAmount()).isZero();
            assertThat(escrow.getRefundedAmount()).isEqualTo(12_000L);
            assertThat(escrow.getRefundedAt()).isEqualTo(now);
        }
    }
}
